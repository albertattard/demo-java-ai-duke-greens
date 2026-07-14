locals {
  application_name     = "duke-greens"
  display_name         = "Duke Greens"
  domain_name          = "javaworkshopshub.dev"
  availability_domain  = data.oci_identity_availability_domains.available.availability_domains[0].name
  ssh_private_key_path = pathexpand(var.ssh_private_key_file)
}

data "oci_identity_availability_domains" "available" {
  compartment_id = var.tenancy_ocid
}

data "oci_core_images" "oracle_linux" {
  compartment_id           = var.compartment_ocid
  operating_system         = "Oracle Linux"
  operating_system_version = "10"
  shape                    = "VM.Standard.E5.Flex"
  sort_by                  = "TIMECREATED"
  sort_order               = "DESC"
}

resource "oci_core_vcn" "application" {
  compartment_id = var.compartment_ocid
  display_name   = "${local.display_name} VCN"
  cidr_blocks    = ["10.42.0.0/16"]
  dns_label      = "dukegreens"
}

resource "oci_core_internet_gateway" "application" {
  compartment_id = var.compartment_ocid
  display_name   = "${local.display_name} Internet Gateway"
  vcn_id         = oci_core_vcn.application.id
  enabled        = true
}

resource "oci_core_route_table" "application" {
  compartment_id = var.compartment_ocid
  display_name   = "${local.display_name} Public Routes"
  vcn_id         = oci_core_vcn.application.id

  route_rules {
    destination       = "0.0.0.0/0"
    destination_type  = "CIDR_BLOCK"
    network_entity_id = oci_core_internet_gateway.application.id
  }
}

resource "oci_core_network_security_group" "application" {
  compartment_id = var.compartment_ocid
  display_name   = "${local.display_name} Public Ingress"
  vcn_id         = oci_core_vcn.application.id
}

resource "oci_core_network_security_group_security_rule" "http" {
  network_security_group_id = oci_core_network_security_group.application.id
  direction                 = "INGRESS"
  protocol                  = "6"
  source                    = "0.0.0.0/0"
  source_type               = "CIDR_BLOCK"

  tcp_options {
    destination_port_range {
      min = 80
      max = 80
    }
  }
}

resource "oci_core_network_security_group_security_rule" "https" {
  network_security_group_id = oci_core_network_security_group.application.id
  direction                 = "INGRESS"
  protocol                  = "6"
  source                    = "0.0.0.0/0"
  source_type               = "CIDR_BLOCK"

  tcp_options {
    destination_port_range {
      min = 443
      max = 443
    }
  }
}

resource "oci_core_network_security_group_security_rule" "ssh" {
  network_security_group_id = oci_core_network_security_group.application.id
  direction                 = "INGRESS"
  protocol                  = "6"
  source                    = var.ssh_allowed_cidr
  source_type               = "CIDR_BLOCK"

  tcp_options {
    destination_port_range {
      min = 22
      max = 22
    }
  }
}

resource "oci_core_network_security_group_security_rule" "egress" {
  network_security_group_id = oci_core_network_security_group.application.id
  direction                 = "EGRESS"
  protocol                  = "all"
  destination               = "0.0.0.0/0"
  destination_type          = "CIDR_BLOCK"
}

resource "oci_core_subnet" "application" {
  compartment_id             = var.compartment_ocid
  display_name               = "${local.display_name} Public Subnet"
  vcn_id                     = oci_core_vcn.application.id
  cidr_block                 = "10.42.0.0/24"
  route_table_id             = oci_core_route_table.application.id
  prohibit_public_ip_on_vnic = false
  prohibit_internet_ingress  = false
  dns_label                  = "public"
}

resource "oci_core_instance" "application" {
  availability_domain = local.availability_domain
  compartment_id      = var.compartment_ocid
  display_name        = "${local.display_name} Application"
  shape               = "VM.Standard.E5.Flex"

  shape_config {
    ocpus         = 1
    memory_in_gbs = 6
  }

  create_vnic_details {
    assign_public_ip = true
    display_name     = local.application_name
    nsg_ids          = [oci_core_network_security_group.application.id]
    subnet_id        = oci_core_subnet.application.id
  }

  metadata = {
    ssh_authorized_keys = join("\n", var.ssh_authorized_keys)
  }

  source_details {
    source_id               = data.oci_core_images.oracle_linux.images[0].id
    source_type             = "image"
    boot_volume_size_in_gbs = 50
  }
}

resource "terraform_data" "application" {
  depends_on = [oci_core_network_security_group_security_rule.ssh]

  triggers_replace = [
    filesha256(abspath(var.application_jar_file)),
    filesha256("${path.module}/templates/bootstrap.sh.tftpl"),
  ]

  connection {
    type           = "ssh"
    host           = oci_core_instance.application.public_ip
    user           = "opc"
    agent          = var.ssh_private_key_use_agent
    agent_identity = var.ssh_private_key_use_agent ? local.ssh_private_key_path : null
    private_key    = var.ssh_private_key_use_agent ? null : file(local.ssh_private_key_path)
    timeout        = "10m"
  }

  provisioner "remote-exec" {
    inline = ["cloud-init status --wait || true"]
  }

  provisioner "file" {
    content = templatefile("${path.module}/templates/bootstrap.sh.tftpl", {
      domain_name = local.domain_name
    })
    destination = "/tmp/duke-greens-bootstrap.sh"
  }

  provisioner "remote-exec" {
    inline = ["sudo bash /tmp/duke-greens-bootstrap.sh"]
  }

  provisioner "file" {
    source      = abspath(var.application_jar_file)
    destination = "/tmp/duke-greens.jar"
  }

  provisioner "file" {
    source      = pathexpand(var.application_config_file)
    destination = "/tmp/duke-greens-application.yml"
  }

  provisioner "remote-exec" {
    inline = [
      "sudo install --owner=duke-greens --group=duke-greens --mode=0640 /tmp/duke-greens.jar /opt/duke-greens/duke-greens.jar",
      "sudo install --owner=root --group=duke-greens --mode=0640 /tmp/duke-greens-application.yml /etc/duke-greens/application.yml",
      "rm -f /tmp/duke-greens.jar /tmp/duke-greens-application.yml",
      "sudo systemctl enable duke-greens.service",
      "sudo systemctl restart duke-greens.service",
      "curl --fail --silent --show-error --retry 10 --retry-connrefused --retry-delay 1 http://127.0.0.1:8080/ >/dev/null",
    ]
  }
}

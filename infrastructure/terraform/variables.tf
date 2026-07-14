variable "region" {
  type        = string
  default     = "eu-frankfurt-1"
  description = "OCI region for the Duke Greens deployment."
}

variable "tenancy_ocid" {
  type        = string
  description = "OCI tenancy OCID."
}

variable "compartment_ocid" {
  type        = string
  description = "OCI compartment OCID in which to create the deployment."
}

variable "ssh_authorized_keys" {
  type        = list(string)
  description = "Public SSH keys authorised for the opc account."
}

variable "ssh_private_key_file" {
  type        = string
  description = "Path to the local private key matching one of ssh_authorized_keys."
}

variable "ssh_private_key_use_agent" {
  type        = bool
  default     = true
  description = "Use ssh-agent for Terraform provisioning. Enable this for passphrase-protected keys after loading the key with ssh-add."
}

variable "ssh_allowed_cidr" {
  type        = string
  description = "Single trusted public IPv4 CIDR permitted to reach SSH, for example 203.0.113.10/32."

  validation {
    condition     = can(cidrhost(var.ssh_allowed_cidr, 0))
    error_message = "ssh_allowed_cidr must be a valid IPv4 or IPv6 CIDR."
  }
}

variable "application_jar_file" {
  type        = string
  default     = "../../target/duke-greens-1.0.0.jar"
  description = "Path, relative to this Terraform directory, to the packaged application JAR."
}

variable "application_config_file" {
  type        = string
  description = "Path to the untracked Spring YAML file containing the OpenAI API key and BCrypt demo-access-code hash. Its contents are never Terraform input."
}

output "application_public_ip" {
  description = "Create an A record for javaworkshopshub.dev pointing to this address."
  value       = oci_core_instance.application.public_ip
}

output "application_instance_ocid" {
  description = "OCID of the Duke Greens application instance."
  value       = oci_core_instance.application.id
}

output "application_url" {
  description = "Public URL after the DNS A record has propagated and Caddy has issued its certificate."
  value       = "https://javaworkshopshub.dev"
}

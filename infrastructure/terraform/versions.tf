terraform {
  required_version = ">= 1.11.0"

  required_providers {
    oci = {
      source  = "oracle/oci"
      version = "~> 7.18"
    }
  }
}

provider "oci" {
  region = var.region
}

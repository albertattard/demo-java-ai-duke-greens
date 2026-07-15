#!/usr/bin/env bash

set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "$repository_root"
./mvnw clean verify

cd infrastructure/terraform
terraform apply -auto-approve -replace=terraform_data.application

curl --fail --location https://javaworkshopshub.dev/
curl --fail --location https://javaworkshopshub.dev/demo-access

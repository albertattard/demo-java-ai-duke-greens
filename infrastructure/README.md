# OCI deployment

This directory contains the Terraform configuration for the single-VM Duke Greens deployment at `javaworkshopshub.dev`.

One small AMD-based OCI VM is sufficient for this demo, and a load balancer would add cost and operational surface without adding useful visitor value. The VM uses Oracle Linux 10 and installs Oracle JDK 25’s `jdk-25-headless` package from the Oracle Linux repository. Caddy terminates TLS and proxies only to the application’s loopback listener. Terraform creates an isolated VCN, a public subnet, a network security group, and the VM.

## Prerequisites

- Terraform 1.11 or later.
- OCI CLI configuration that can create Compute and Networking resources in the selected compartment.
- An SSH key pair. The public key is supplied to the VM. For a passphrase-protected private key, set `ssh_private_key_use_agent = true` and load it in the local SSH agent with `ssh-add` before provisioning.
- A packaged application JAR. Run `./mvnw verify` from the repository root before applying Terraform.
- The existing untracked Spring configuration file containing the OpenAI API key and BCrypt demo-access-code hash. Terraform copies this file over SSH during provisioning; it never reads the values into its state.
- Control of the DNS zone for `javaworkshopshub.dev`.

## Configure

Create the ignored `terraform.tfvars` file locally. Set `tenancy_ocid`, `compartment_ocid`, `ssh_authorized_keys`, `ssh_private_key_file`, `ssh_allowed_cidr`, and `application_config_file`. The optional `region`, `ssh_private_key_use_agent`, and `application_jar_file` values have defaults in [`variables.tf`](terraform/variables.tf). Do not add API keys or access codes to `terraform.tfvars`.

```shell
cd infrastructure/terraform
```

The `application_config_file` value must point to the existing untracked Spring YAML configuration, normally `~/.demo/demo-java-ai-duke-greens.yml`. Its contents are copied directly to the VM and do not become Terraform input values. Terraform expands `~` for this value.

## Provision

```shell
terraform fmt
terraform init
terraform plan
terraform apply
```

Terraform outputs the public IP. Create or update an A record for `javaworkshopshub.dev` to that address. Caddy obtains and renews the TLS certificate once public DNS resolves to the VM and ports 80 and 443 reach it. Do not use a proxied DNS mode that prevents HTTP-01 validation unless it is configured to pass the challenge through.

After DNS has propagated, verify:

```shell
curl --fail --location https://javaworkshopshub.dev/
curl --fail --location https://javaworkshopshub.dev/demo-access
```

## Redeploy

Package the application, then force only the provisioning resource to rerun. Infrastructure resources remain unchanged.

```shell
./mvnw verify
cd infrastructure/terraform
terraform apply -replace=terraform_data.application
```

The deployment intentionally retains session state only in application memory. A redeploy or VM restart ends active visitor sessions, as documented in [ADR-0004](../docs/adr/ADR-0004-keep-active-visitor-state-in-memory.md).

## Connect and inspect logs

Replace `~/.ssh/oracle_id_ed25519` with the path to the private key that matches an entry in `ssh_authorized_keys`. From the repository root, connect to the VM with:

```shell
ssh -i ~/.ssh/oracle_id_ed25519 "opc@$(terraform -chdir=infrastructure/terraform output -raw application_public_ip)"
```

Retrieve the Duke Greens application’s most recent 1,000 log entries without first opening an interactive SSH session with:

```shell
ssh -i ~/.ssh/oracle_id_ed25519 "opc@$(terraform -chdir=infrastructure/terraform output -raw application_public_ip)" 'sudo journalctl --unit=duke-greens.service --lines=1000'
```

Follow the Duke Greens application’s live logs without first opening an interactive SSH session with:

```shell
ssh -i ~/.ssh/oracle_id_ed25519 "opc@$(terraform -chdir=infrastructure/terraform output -raw application_public_ip)" 'sudo journalctl --unit=duke-greens.service --follow'
```

Press `Ctrl+C` to stop following logs. The application runs as the `duke-greens.service` systemd service; its standard output and error are collected by the system journal.

## Operational limits

This is a one-VM demonstration deployment, not a highly available production service. A VM failure or maintenance restart interrupts active sessions. It also does not add an application-aware web-application firewall; if the deployment is exposed beyond supervised demo use, add an OCI WAF or equivalent rate limiting before broad sharing.

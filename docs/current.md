# Current change

## Outcome

Visitors can use Duke Greens securely at `https://javaworkshopshub.dev`, served by a small OCI virtual machine whose infrastructure is managed from this repository with Terraform.

## Constraints

- Keep all OCI infrastructure under `infrastructure/`.
- Use one small x86_64 OCI `VM.Standard.E5.Flex` VM, not a load balancer.
- Terminate and enforce HTTPS at Caddy; bind the Java application only to the VM loopback interface.
- Expose only HTTP, HTTPS, and SSH restricted to the deployment operator’s CIDR.
- Configure forwarded headers and secure, HttpOnly, `SameSite=Lax` session cookies for the proxied deployment.
- Install Oracle JDK 25 from the Oracle Linux 10 repository rather than an OpenJDK distribution.
- Use the provided Falcon Heartbeat validation checks during VM bootstrap; install or repair the agent only when it is not healthy.
- Keep the OpenAI API key and demo-access-code hash out of source control, Terraform variables, Terraform state, logs, and command history; upload the existing untracked Spring configuration only during provisioning.
- Do not replace another application: `javaworkshopshub.dev` is currently unused.

## Done when

- Terraform can create the VM, its isolated network, and narrowly scoped ingress rules using the operator’s OCI credentials.
- Provisioning uploads the packaged JAR and the existing external Spring configuration, starts the application as an unprivileged systemd service, and installs Caddy as the public HTTPS proxy.
- VM bootstrap validates the Falcon package, service, established connection, and reduced-functionality mode; when any check fails, it installs Python 3 and pip, runs the supplied Falcon installer over HTTPS, removes the temporary installer, and requires all checks to pass afterward.
- Terraform outputs the public IP required for the DNS A record and the deployment instructions make the DNS dependency explicit.
- The normal test suite and Terraform formatting and validation pass.

## Verification

- Baseline: `./mvnw verify` passed, including 23 browser integration tests.
- `terraform fmt -recursive` completed and `terraform validate` passed with the official OCI provider v7.32.0.
- `terraform plan` passes for an Oracle Linux 10 `VM.Standard.E5.Flex` image in the selected Frankfurt compartment; it proposes 11 resources.
- Terraform created the OCI infrastructure and provisioned the application on `130.61.40.43`. The application starts as the unprivileged `duke-greens` user, responds on loopback port 8080, and Caddy returns the expected HTTP-to-HTTPS redirect.
- The deployed JVM uses `-Xmx512m`. Provisioning restarts the service after replacing the JAR or external configuration, and the loopback health check passed after that restart.
- The VM firewall allows HTTP and HTTPS alongside the Terraform-managed OCI ingress rules. Public DNS resolves `javaworkshopshub.dev` to `130.61.40.43`; Let’s Encrypt validated the domain and public HTTPS returned `200` with a valid certificate.
- Baseline verification for this Falcon change could not complete in the sandbox: browser integration tests cannot bind a random local port (`SocketException: Operation not permitted`).
- The bootstrap template passed `bash -n`, and `terraform fmt -check -recursive` passed. `terraform validate` could not start the installed OCI provider in this runtime, which exits before returning its plugin schema.
- The conditional Falcon bootstrap update passed `bash -n`, `terraform fmt -check -recursive`, and `git diff --check`.

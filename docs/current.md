# Current change

## Outcome

Deployments run Terraform without an interactive approval prompt.

## Constraints

- Change only `tools/deplo.sh`.

## Done when

- The script invokes `terraform apply -auto-approve -replace=terraform_data.application`.

## Verification

- Baseline: `./mvnw verify` passed, including 21 end-to-end tests.
- `bash -n tools/deplo.sh` passed.
- `git diff --check` passed.

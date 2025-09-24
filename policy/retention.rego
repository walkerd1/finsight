package finsight.retention

deny[msg] {
  input.kind == "Bucket"
  not input.spec.ttl_days
  msg := sprintf("Bucket %s missing ttl_days", [input.metadata.name])
}

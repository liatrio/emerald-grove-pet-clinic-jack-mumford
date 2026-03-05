# 01 Questions Round 2 - AWS Core Infrastructure (Follow-up)

Thank you for your answers! I need a couple of clarifications before generating the spec.

## 1. AWS Region (Required)

Which AWS region should we deploy to?

- [x] (A) us-east-1 (N. Virginia) - Most services, lowest cost
- [ ] (B) us-east-2 (Ohio) - Good balance, newer region
- [ ] (C) us-west-1 (N. California) - West coast US
- [ ] (D) us-west-2 (Oregon) - Popular west coast choice
- [ ] (E) eu-west-1 (Ireland) - Europe primary
- [ ] (F) eu-central-1 (Frankfurt) - Europe GDPR-friendly
- [ ] (G) ap-southeast-1 (Singapore) - Asia Pacific
- [ ] (H) Other (specify): _____________

**Your choice:** _____________

## 2. Multi-AZ Clarification

For the "start minimal, scale up" approach with staging + production environments:

- [x] (A) Single AZ for both staging and production initially (most cost-effective)
- [ ] (B) Single AZ for staging, Multi-AZ for production (balanced approach)
- [ ] (C) Multi-AZ for both environments (higher availability, higher cost)

**Recommendation:** Option A or B aligns with "start minimal, scale up as needed"

## 3. Terraform Backend Bootstrap

The S3 backend for Terraform state requires an S3 bucket and DynamoDB table. Should the spec include:

- [x] (A) Instructions for manual creation of S3 bucket and DynamoDB table (one-time setup)
- [ ] (B) Separate bootstrap Terraform config to create state backend resources
- [ ] (C) Assume these already exist (provide names)

**If (C), please provide:**
- S3 Bucket Name: _____________
- DynamoDB Table Name: _____________

---

**Next Steps:** Please answer these three questions and save the file. Once I receive your answers, I'll proceed directly to generating the specification.

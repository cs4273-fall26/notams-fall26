# CAP-20 mentor review follow-through

Source: [PR #4](https://github.com/cs4273-fall26/notams-fall26/pull/4), reviewed against CAP-20 commit `8c2b1dfae197c1ce51cfc20b8c284273b0e2e0b8`.
All 37 inline discussions were read, including the author's replies and the mentor's follow-up decisions. Existing approvals and review summaries were also checked.

## Changes by discussion

| Discussion | Outcome |
| --- | --- |
| [NmsApiClient.java:34](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4098318446) | Renamed connection and request timeout constants to include IN_SECONDS; expiry margin also names its unit. |
| [NmsApiClient.java:285](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4098351421) | Input/configuration failures now use IllegalArgumentException before any HTTP request. |
| [ApiLayerDemo.java:21](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4098369877) | Removed narration comments; kept demo context and required environment variables in its class documentation and README. |
| [NmsApiClient.java:95](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4098381339) | NmsConfiguration collects all missing variables into one message, including production URLs when selected. |
| [NmsApiClient.java:120](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4098394927) | Identical normalized route endpoints yield one request and one RawNotamResponse. |
| [NmsApiClient.java:108](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4098397918) | Route method parameters are final. |
| [NmsApiClient.java:115](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4098399140) | Route local values are final. |
| [NmsApiClient.java:118](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4098403430) | Removed the duplicate end-JSON branch; the remaining end response is final. |
| [NmsApiClient.java:107](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4098415056) | Both public fetch methods return List<RawNotamResponse>. |
| [NmsApiClient.java:342](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4098428723) | Kept synchronized token access as permitted; added a concurrent-request test. |
| [RawRouteResponses.java:28](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4098458855) | Replaced RawRouteResponses with an immutable RawNotamResponse record with final constructor parameters. |
| [NmsApiClient.java:292](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4098479534) | LocationIdentifier performs normalization and syntax validation at the input boundary. Demo constructs both identifiers before calling the API; client receives validated values. |
| [NmsApiClient.java:266](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4098488269) | Catch parameters are final. |
| [NmsApiClient.java:1](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4098516448) | Added automated local-server tests for the client plus configuration and input tests; added Java 17/21 CI. |
| [ApiLayerDemo.java:22](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4099674315) | Demo client is final. |
| [ApiLayerDemo.java:33](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4099680338) | Renamed the preview helper to printResponsePreview. |
| [ApiLayerDemo.java:30](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4099686290) | Demo labels start/end responses and labels a shared endpoint once. |
| [ApiLayerDemo.java:42](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4099687412) | Demo catch parameters are final. |
| [NmsApiClient.java:289](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4099707983) | Used Apache Commons StringUtils for input and credential cleanup. |
| [NmsApiException.java:17](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4099714196) | Removed the misleading invalid-airport API exception example; invalid syntax fails before the API call. HTTP rejection of a syntactically valid code still preserves status. |
| [NmsApiException.java:19](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4099715720) | Exception constructor parameters are final. |
| [NmsApiException.java:12](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4099719189) | Added NmsHttpException for actual HTTP failures; base NmsApiException no longer stores a sentinel status. |
| [NmsApiClient.java:17](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4099720845) | Expanded NMS to NOTAM Management Service in client documentation and README. |
| [NmsApiClient.java:30](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4099731704) | Added staging/production selection and environment-specific endpoint configuration. Production requires FAA-provided URLs; it cannot silently use staging defaults. |
| [NmsApiClient.java:41](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4099736472) | Replaced token regexes with Jackson JSON parsing and added malformed/escaped/numeric/string-field tests. |
| [NmsApiClient.java:53](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4099744629) | Investigated available FAA public documentation and documented the unresolved cross-process token policy and a concrete follow-up procedure below. Per-client concurrency is tested; provider-wide concurrency is not claimed. |
| [NmsApiClient.java:52](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4105030883) | Updated cache comment to say every request. |
| [NmsApiClient.java:71](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4105068311) | Trimmed both client ID and client secret; a request-level test checks the resulting Basic credentials. |
| [NmsApiClient.java:59](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4105074392) | Moved environment loading into NmsConfiguration. NmsApiClient has one public construction path for supplied credentials/endpoints and an internal test seam. |
| [ApiLayerDemo.java:14](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4105265161) | Clarified that the demo consumes command-line arguments and is separate from CAP-19 interactive input. |
| [NmsApiClient.java:4](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4105335296) | Enforced JDK 17+ with Maven and compiler release 17, supplied a pinned Maven wrapper, and documented IDE/JAVA_HOME setup. |
| [NmsApiClient.java:61](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4105343718) | Used StringUtils.isBlank to simplify null/blank checks. |
| [NmsApiClient.java:133](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4105359640) | Single-location parameter is final. |
| [NmsApiClient.java:143](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4105397732) | Shortened retry comment to explain why a 401 matters rather than restating the steps. |
| [NmsApiClient.java:151](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4105399781) | Removed the obvious successful-response comment. |
| [NmsApiClient.java:162](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4105408239) | Request location parameter is final. |
| [NmsApiClient.java:172](https://github.com/cs4273-fall26/notams-fall26/pull/4#discussion_r4105410708) | Request construction locals are final. |

## Token concurrency investigation

The cache is instance-local. Synchronization prevents simultaneous refreshes within one client;
the automated concurrent-request test verifies that eight successful requests reuse one token.
It does not establish how FAA handles separate clients/processes using the same credentials.

The [FAA NMS portal](https://nms.aim.faa.gov/) and
[FAA NMS FAQ](https://www.faa.gov/about/initiatives/notam/faqs) provide API access/documentation
contact routes. The public pages reviewed do not establish whether issuing a new token revokes
an existing token for the same client credentials. Do not assume either behavior or add a
process-local global cache and call that a cross-process solution.

Follow-up requiring the team's FAA documentation or staging access:
1. Confirm the provider's documented concurrent-token policy and token limits.
2. In staging, request token A and make a NOTAM request; request token B with the same
   credentials, then retry A and B. Record statuses, never tokens or secrets.
3. Repeat with two processes to observe whether refreshes invalidate the other process.
4. If tokens coexist, retain per-client caches. If they revoke each other, agree on separate
   development credentials or a shared token service before multi-process use.

This is a documented investigation/follow-up, not a claim that FAA behavior has been verified
and not a newly created GitHub/Jira ticket.

## Validation

Local Maven verification: 54 tests, 0 failures, 0 errors, 0 skipped, using JDK 21 and compiler
release 17. Tests use a loopback HTTP server and synthetic credentials. CI runs the same
suite on JDK 17 and 21. No live FAA request was made.

## Integration changes

Callers now create `LocationIdentifier` values before calling the API. Both fetch methods return
`List<RawNotamResponse>`; callers iterate that list and pass each `rawJson()` to the parsing
layer. The previous `RawRouteResponses` start/end container has been removed. Configuration
loading is now `NmsConfiguration.fromEnvironment()`. Code that needs HTTP status catches
`NmsHttpException`; transport/response-processing failures remain `NmsApiException`.

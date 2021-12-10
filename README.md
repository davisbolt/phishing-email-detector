# phishing-email-detector
An application for detecting phishing emails.

## Build
Requires Gradle.

`gradle build`

## Usage
For individual email files:

`gradle run --args="<email file path>"`

For running a batch scan on a folder conataining email files:

`gradle run --args="<folder path>"`

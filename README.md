# Voice Bridge Java Sample
<a href="http://dev.bandwidth.com"><img src="https://s3.amazonaws.com/bwdemos/BW-VMP.png"/></a>
</div>

 # Table of Contents

<!-- TOC -->

- [Voice Bridge Java Sample](#voice-bridge-java-sample)
- [Description](#description)
- [Bandwidth](#bandwidth)
- [Environmental Variables](#environmental-variables)
- [Callback URLs](#callback-urls)
    - [Ngrok](#ngrok)

<!-- /TOC -->

# Description
This sample app shows how to hide (mask) a phone number through a Bandwidth voice API using Bandwidth's Bridge BXML verb. After setting up your Bandwidth Application to point to the URL running this application, anyone who calls your Bandwidth phone number will be bridged to the masked number without either party members knowing the other's phone number.

# Bandwidth

In order to use the Bandwidth API users need to set up the appropriate application at the [Bandwidth Dashboard](https://dashboard.bandwidth.com/) and create API tokens.

To create an application log into the [Bandwidth Dashboard](https://dashboard.bandwidth.com/) and navigate to the `Applications` tab.  Fill out the **New Application** form selecting the voice service.  All Bandwidth services require publicly accessible Callback URLs, for more information on how to set one up see [Callback URLs](#callback-urls).

For more information about API credentials see [here](https://dev.bandwidth.com/guides/accountCredentials.html#top)

# Environmental Variables
The sample app uses the below environmental variables.
```sh
BW_ACCOUNT_ID                 # Your Bandwidth Account Id
BW_USERNAME                   # Your Bandwidth API Token
BW_PASSWORD                   # Your Bandwidth API Secret
BW_NUMBER                     # Your The Bandwidth Phone Number (E164 Format)
USER_NUMBER                   # A phone number to mask with the BW_NUMBER (E164 Format)
BW_VOICE_APPLICATION_ID       # Your Voice Application Id created in the dashboard
BASE_CALLBACK_URL             # Your public base url
```

# Callback URLs

For a detailed introduction to Bandwidth Callbacks see https://dev.bandwidth.com/guides/callbacks/callbacks.html

Below are the callback paths:
* `/callbacks/outbound`
* `/callbacks/inbound`

## Ngrok

A simple way to set up a local callback URL for testing is to use the free tool [ngrok](https://ngrok.com/).  
After you have downloaded and installed `ngrok` run the following command to open a public tunnel to your port (`$PORT`)
```cmd
ngrok http $PORT
```
You can view your public URL at `http://127.0.0.1:{PORT}` after ngrok is running.  You can also view the status of the tunnel and requests/responses here.

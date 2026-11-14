# Banglar AI — Privacy Policy Draft

**Status: Draft only. This document must be reviewed and completed before public release.**

_Last updated: 14 November 2026_

## Current prototype data handling

The current development prototype stores chat history in private app storage on the user's Android device. The Groq API key entered by the user is encrypted at rest using Android Keystore. When the user sends a message, the app sends the recent conversation context to the configured Groq API endpoint to generate a response.

The current prototype does not provide Banglar AI user accounts, Banglar AI-operated cloud backup, or a Banglar AI-operated analytics service.

## Optional device permissions

- Microphone: used when the user activates voice input.
- Camera: requested for the flashlight feature in the current implementation; camera capture is launched through Android's camera app.
- Overlay: used only when the user enables the floating assistant.
- Accessibility: optional. The service is intended to inspect visible text or activate a matching visible item only after an explicit user command. It must not be used for covert monitoring or unrelated data collection.

## Third-party AI processing

Messages sent to the configured AI provider are processed by that provider under its own terms and privacy policy. Users should not submit passwords, financial secrets, or other highly sensitive information.

## User controls

Users can clear local chat history, disable optional features, revoke Android permissions in system settings, and uninstall the app.

## Before publication, complete these sections

- Legal publisher name and contact email.
- Public support contact and website.
- Exact AI provider and data-processing terms.
- Data categories, purposes, legal basis, retention, and deletion process.
- Whether accounts, analytics, crash reporting, advertising, or subscriptions are added.
- Applicable regional privacy rights and contact process.
- Effective date and policy URL.

Do not publish this draft as-is. Update it to match the final production app and backend.

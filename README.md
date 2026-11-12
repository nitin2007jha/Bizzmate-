# My Business ERP — Android Application

A full-featured native Android ERP application for Indian MSMEs, retailers, and distributors built with **Kotlin** and **Jetpack Compose (Material 3)**.

## Core Features

- **Dashboard & Business Analytics**: Real-time tracking of today's collection (Cash vs UPI), bill counts, pending receivables, and low-stock alerts.
- **GST Invoicing & Billing**:
  - Intra-State (CGST + SGST) and Inter-State (IGST) tax calculation modes.
  - Inclusive and exclusive GST pricing support.
  - Line-item discounts, shipping charges, and automated HSN code assignment.
  - One-tap WhatsApp invoice sharing with pre-formatted tax invoice summaries.
  - Payment tracking (Paid, Unpaid, Partial) with Cash, UPI, Bank Transfer, and Cheque options.
- **Inventory & Stock Management**:
  - Live stock tracking with customizable low-stock threshold alerts.
  - Quick stock adjustments (+ / - stock with reason).
  - Categorization, SKU, purchase price, MRP, and sale price tracking.
- **Customer & Client Ledger**:
  - Customer directory with address, phone, GSTIN, and opening balances.
  - Direct communication actions: 1-tap Call and WhatsApp chat.
  - Client transaction history and pending balance calculation.
- **Expense Tracking**:
  - Categorized business expenses (Utilities, Rent, Salaries, Raw Materials, Packaging, Logistics).
  - Reference number logging and payment mode tracking.
- **CA Tax Portal & GSTR-1 Summary**:
  - Tax turnover and GST breakdown by tax slabs (5%, 12%, 18%, 28%).
  - One-tap sharing of tax audit reports formatted for Chartered Accountants.
- **Catalog Studio & Digital Store**:
  - Digital product catalog with discount badges and pricing.
  - Shareable catalog summaries and direct WhatsApp order links.
- **Bilingual Hindi & English Voice Assistant**:
  - Natural voice/text query processing for commands like:
    - *"आज की सेल"* (Today's collection breakdown)
    - *"कम स्टॉक"* (Low stock alert items)
    - *"जीएसटी रिपोर्ट"* (Current GST tax output)
    - *"नया बिल"* (Open invoice creator)
    - *"ग्राहक"* (Open clients directory)
- **Data Export & Backup**:
  - Complete data export across invoices, stock, clients, and expenses via the Android Share sheet.

## Tech Stack & Architecture

- **Language:** Kotlin 2.1
- **UI Framework:** Jetpack Compose with Material Design 3
- **Architecture:** MVVM / Clean Architecture with Kotlin Coroutines and StateFlow
- **Minimum SDK:** Android 7.0 (API 24)
- **Target SDK:** Android 16 (API 36)
- **Design System:** Custom Material 3 theme with dynamic color schemes and high contrast accessibility

# 🧾 SmartReceipt AI 
### Contextual Expense Verification Engine

**SmartReceipt AI** is a full-stack Spring Boot application that leverages AI to extract and verify financial data from receipts and invoices. 

## 🧠 The Intelligence
Unlike standard OCR scanners, this engine uses a **Contextual Parsing Algorithm** I developed to:
* **Filter Metadata:** Ignores misleading values like "Cash Provided," "Change Given," or "Subtotal" to find the true Grand Total.
* **Bottom-Up Search:** Scans documents in reverse to accurately identify totals on complex business invoices where headers often contain similar keywords.
* **Format Resilience:** Handles international decimal formats (commas vs. dots) seamlessly.

## 🛠️ Tech Stack
* **Backend:** Java 17, Spring Boot, Maven
* **Frontend:** HTML5, CSS3 (Modern UI), Vanilla JavaScript
* **AI:** OCR.space Engine 2 (Advanced Document Logic)

## 🚀 Key Security Features
* **Environment-Based Config:** Implemented `@Value` properties to ensure API credentials remain local and never reach public version control.
* **Clean History:** Managed `.gitignore` to maintain a professional, production-ready repository.

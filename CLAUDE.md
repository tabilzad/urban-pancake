# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Receipt Printer Hackathon project that allows participants to build a drag-and-drop receipt designer in JavaScript/TypeScript and write a Kotlin interpreter to process the generated JSON DSL format on real Epson printers.

**Core Goal from pancakeinstructions.md:**
1. Read the desired_receipt.txt - the target receipt format to replicate
2. Design a JSON schema that can represent receipt content 
3. Design a drag and drop builder for receipt creation
4. Write a Kotlin script that reads the JSON and calls Epson printer functions. When parsing json, use JsonObject. Do not include any imports in the script.

## Development Commands

### Next.js Frontend
- `npm run dev` - Start development server with Turbopack (opens on http://localhost:3000)
- `npm run dev:debug` - Start development server with debug logging
- `npm run build` - Build production version
- `npm run start` - Start production server
- `npm run lint` - Run ESLint

### Network Requirements
⚠️ **IMPORTANT**: Must be connected to `192.168.29.*` network for server communication and printer access during hackathon.

## Architecture Overview

### Frontend Structure
- **Next.js 15** with React 19, TypeScript, and Tailwind CSS
- **Main Interface**: 4-tab layout (Rules, Design, Preview, Submit)
- **Receipt Designer** (`src/components/ReceiptDesigner.tsx`): Drag-drop interface with element palette, canvas, properties panel, and JSON preview
- **Receipt Preview** (`src/components/ReceiptPreview.tsx`): JavaScript interpreter for local testing with HTML Canvas
- **Kotlin Submission** (`src/components/KotlinSubmission.tsx`): Code editor and server submission interface

### JSON DSL Format
The system uses a standardized JSON format defined in `docs/json-dsl-spec.md`:
- **Elements array**: Sequential receipt elements
- **Types**: text, barcode, qrcode, divider, dynamic, feed, image
- **Styling**: size (SMALL/NORMAL/LARGE/XLARGE), bold, underline
- **Dynamic fields**: {store_name}, {order_number}, {item_list}, {total}, etc.

### Key Components
- **ReceiptDesigner**: Full drag-drop interface with palette, canvas, properties panel, and sliding JSON preview
- **MonacoEditor**: Code editor with JavaScript/Kotlin syntax highlighting
- **PrinterDemo**: Canvas-based receipt rendering for preview
- **Element Types**: Complete implementation of all JSON DSL element types

### Kotlin Integration
- **kotlin-examples/**: Template files, mock printer, and sample receipts
- **Server Integration**: Submit Kotlin interpreters to Android server via API
- **MockEpsonPrinter.kt**: Local testing with console output
- **InterpreterTemplate.kt**: Starter code structure for participants
- **printerapi.md**: Complete EpsonPrinter API reference with all available methods, type definitions, and examples
- **docs/order-structure.md**: Order object schema with real data structure (orderId, items, totals, customer info, promotions)

### Target Receipt Format
Based on `desired_receipt.txt`: Restaurant receipt with header, items, calculations, footer, and structured formatting.

## File Locations

## Testing Strategy

### Local Development
1. Use `ReceiptPreview` with JavaScript interpreter for immediate feedback
2. Test JSON generation with drag-drop designer
3. Use `MockEpsonPrinter.kt` for Kotlin interpreter testing

### Server Integration  
1. Upload Kotlin interpreter via UI
2. Receive team-specific endpoint
3. Test with real printer during judging phase

## Development Notes

- The project uses Turbopack for fast development builds
- Monaco Editor provides syntax highlighting for both JavaScript and Kotlin
- Real printer functionality is controlled server-side during judging
- Network dependency on hackathon WiFi for server communication
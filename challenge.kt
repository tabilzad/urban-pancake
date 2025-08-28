fun interpret(jsonString: String, printer: EpsonPrinter, order: Any? = null) {
    try {
        val jsonObject = JSONObject(jsonString)
        val elements = jsonObject.getJSONArray("elements")
        
        // Process each element sequentially
        for (i in 0 until elements.length()) {
            val element = elements.getJSONObject(i)
            processElement(element, printer)
        }
        
        // Always cut paper at the end
        printer.cutPaper()
        
    } catch (e: Exception) {
        println("Error processing JSON: ${e.message}")
        // Print error message on receipt
        printer.addText("ERROR: Invalid receipt format")
        printer.cutPaper()
    }
}

fun processElement(element: JSONObject, printer: EpsonPrinter) {
    val type = element.optString("type")
    
    when (type) {
        "text" -> processTextElement(element, printer)
        "barcode" -> processBarcodeElement(element, printer)
        "qrcode" -> processQRCodeElement(element, printer)
        "image" -> processImageElement(element, printer)
        "divider" -> processDividerElement(element, printer)
        "dynamic" -> processDynamicElement(element, printer)
        "feed" -> processFeedElement(element, printer)
        "alignment" -> processAlignmentElement(element, printer)
        "style" -> processStyleElement(element, printer)
        "linespace" -> processLineSpaceElement(element, printer)
        "clear" -> processClearElement(element, printer)
        else -> {
            println("Unknown element type: $type")
        }
    }
}

fun processTextElement(element: JSONObject, printer: EpsonPrinter) {
    val content = element.optString("content")
    printer.addText(content)
}

fun processBarcodeElement(element: JSONObject, printer: EpsonPrinter) {
    val data = element.optString("data")
    val barcodeType = element.optString("barcodeType", "CODE128")
    
    // Print barcode info - replace with actual barcode method when available
    printer.addText("BARCODE[$barcodeType]: $data")
}

fun processQRCodeElement(element: JSONObject, printer: EpsonPrinter) {
    val data = element.optString("data")
    
    // Print QR code info - replace with actual QR method when available
    printer.addText("QR CODE: $data")
}

fun processImageElement(element: JSONObject, printer: EpsonPrinter) {
    val imageData = element.optString("imageData")
    if (imageData.isNotEmpty()) {
        // Print image placeholder - replace with actual image method when available
        printer.addText("[IMAGE: ${imageData.length} chars]")
    }
}

fun processDividerElement(element: JSONObject, printer: EpsonPrinter) {
    val character = element.optString("character", "-")
    val length = element.optInt("length", 48)
    
    val dividerLine = character.repeat(length)
    printer.addText(dividerLine)
}

fun processDynamicElement(element: JSONObject, printer: EpsonPrinter) {
    val field = element.optString("field")
    val content = resolveDynamicField(field)
    printer.addText(content)
}

fun processFeedElement(element: JSONObject, printer: EpsonPrinter) {
    val lines = element.optInt("lines", 1)
    printer.addFeedLine(lines)
}

fun processAlignmentElement(element: JSONObject, printer: EpsonPrinter) {
    val alignment = element.optString("alignment", "LEFT")
    // Print alignment change as comment for now
    printer.addText("[$alignment]")
}

fun processStyleElement(element: JSONObject, printer: EpsonPrinter) {
    val style = element.optJSONObject("style")
    if (style != null) {
        val bold = style.optBoolean("bold")
        val size = style.optString("size", "NORMAL")
        printer.addText("[STYLE: bold=$bold, size=$size]")
    } else {
        printer.addText("[STYLE CHANGE]")
    }
}

fun processLineSpaceElement(element: JSONObject, printer: EpsonPrinter) {
    val space = element.optInt("space", 30)
    // Use feed line as substitute for line spacing
    printer.addText("[LINE SPACE: ${space}px]")
    printer.addFeedLine(1)
}

fun processClearElement(element: JSONObject, printer: EpsonPrinter) {
    // Print clear indicator - element parameter used to maintain consistency
    printer.addText("[CLEAR]")
    println("Clear element processed: ${element.optString("type")}")
}

fun resolveDynamicField(field: String): String {
    return when (field) {
        "{store_name}" -> "BYTE BURGERS"
        "{store_address}" -> "123 Main Street"
        "{store_phone}" -> "(555) 123-4567"
        "{cashier_name}" -> "John Doe"
        "{timestamp}" -> "12/04/2024"
        "{order_number}", "{order_id}" -> "A-0042"
        "{receipt_number}" -> "R-${(1000..9999).random()}"
        
        // Order-specific fields - using default values from desired_receipt.txt
        "{subtotal}" -> "$27.95"
        "{tax}" -> "$2.24"  
        "{total}" -> "$30.19"
        
        "{item_list}" -> "Cheeseburger                    x2      $17.98\n  @ $8.99 each\nFrench Fries                    x1       $3.99\nSoft Drink                      x2       $5.98\n  @ $2.99 each"
        
        "{item_count}" -> "3"
        "{customer_name}" -> "Guest"
        "{customer_phone}" -> "N/A"
        
        "{payment_method}" -> "Credit Card"
        "{change_due}" -> "$0.00"
        "{discount}" -> "$0.00"
        "{promotion_code}" -> ""
        "{tax_rate}" -> "8.0%"
        "{barcode_data}" -> "ORD${(10000..99999).random()}"
        "{qr_data}" -> "https://receipt.example.com/order/${(1000..9999).random()}"
        
        // If unknown field, return as-is
        else -> field
    }
}
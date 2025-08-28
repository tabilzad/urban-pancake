fun interpret(jsonString: String, printer: EpsonPrinter, order: Any? = null) {
    try {
        // First, replace all template variables in the JSON string
        val processedJsonString = replaceTemplateVariables(jsonString, order)
        
        val jsonObject = JSONObject(processedJsonString)
        val elements = jsonObject.getJSONArray("elements")
        
        // Process each element sequentially
        for (i in 0 until elements.length()) {
            val element = elements.getJSONObject(i)
            processElement(element, printer, order)
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

fun processElement(element: JSONObject, printer: EpsonPrinter, order: Any? = null) {
    val type = element.optString("type")
    
    when (type) {
        "text" -> processTextElement(element, printer)
        "barcode" -> processBarcodeElement(element, printer)
        "qrcode" -> processQRCodeElement(element, printer)
        "image" -> processImageElement(element, printer)
        "divider" -> processDividerElement(element, printer)
        "dynamic" -> processDynamicElement(element, printer, order)
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

fun processDynamicElement(element: JSONObject, printer: EpsonPrinter, order: Any? = null) {
    val field = element.optString("field")
    // The field should already be resolved by global template replacement
    // But if it still contains template syntax, resolve it as fallback
    val content = if (field.startsWith("{{") && field.endsWith("}}")) {
        resolveDynamicField(field, order)
    } else {
        field
    }
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

fun replaceTemplateVariables(jsonString: String, order: Any? = null): String {
    var processedString = jsonString
    
    // Define all possible template variables
    val templateVariables = listOf(
        "{{STORE_NAME}}", "{{STORE_NUMBER}}", "{{STORE_ADDRESS}}", "{{STORE_PHONE}}", "{{CASHIER_NAME}}",
        "{{TIMESTAMP}}", "{{ORDER_NUMBER}}", "{{ORDER_ID}}", "{{RECEIPT_NUMBER}}",
        "{{SUBTOTAL}}", "{{TAX}}", "{{TAX_AMOUNT}}", "{{TAX_RATE}}", "{{TOTAL}}", "{{DISCOUNT}}",
        "{{ITEM_LIST}}", "{{ITEM_COUNT}}",
        "{{CUSTOMER_NAME}}", "{{CUSTOMER_ID}}", "{{CUSTOMER_PHONE}}", "{{MEMBER_STATUS}}", "{{LOYALTY_POINTS}}",
        "{{PAYMENT_METHOD}}", "{{CHANGE_DUE}}", "{{PROMOTION_CODE}}",
        "{{BARCODE_DATA}}", "{{QR_DATA}}",
        "{{order_number}}", "{{item_list}}"
    )
    
    // Replace each template variable with its resolved value
    for (template in templateVariables) {
        if (processedString.contains(template)) {
            val resolvedValue = resolveDynamicField(template, order)
            // Escape special JSON characters in the resolved value
            val escapedValue = resolvedValue
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
            processedString = processedString.replace(template, escapedValue)
        }
    }
    
    return processedString
}

fun resolveDynamicField(field: String, order: Any? = null): String {
    return when (field) {
        // Store information
        "{{STORE_NAME}}" -> {
            if (order != null) {
                try {
                    val orderObj = order as JSONObject
                    orderObj.optString("storeName", "BYTE BURGERS")
                } catch (e: Exception) {
                    "BYTE BURGERS"
                }
            } else "BYTE BURGERS"
        }
        
        "{{STORE_NUMBER}}" -> {
            if (order != null) {
                try {
                    val orderObj = order as JSONObject
                    orderObj.optString("storeNumber", "001")
                } catch (e: Exception) {
                    "001"
                }
            } else "001"
        }
        
        "{{STORE_ADDRESS}}" -> "123 Main Street"
        "{{STORE_PHONE}}" -> "(555) 123-4567"
        "{{CASHIER_NAME}}" -> "John Doe"
        
        "{{TIMESTAMP}}" -> {
            if (order != null) {
                try {
                    val orderObj = order as JSONObject
                    val timestamp = orderObj.optLong("timestamp")
                    if (timestamp > 0) {
                        java.text.SimpleDateFormat("MM/dd/yyyy HH:mm").format(java.util.Date(timestamp))
                    } else {
                        "12/04/2024"
                    }
                } catch (e: Exception) {
                    "12/04/2024"
                }
            } else "12/04/2024"
        }
        
        "{{ORDER_NUMBER}}", "{{ORDER_ID}}" -> {
            if (order != null) {
                try {
                    val orderObj = order as JSONObject
                    orderObj.optString("orderId", "A-0042")
                } catch (e: Exception) {
                    "A-0042"
                }
            } else "A-0042"
        }
        
        "{{RECEIPT_NUMBER}}" -> "R-${(1000..9999).random()}"
        
        // Financial fields
        "{{SUBTOTAL}}" -> {
            if (order != null) {
                try {
                    val orderObj = order as JSONObject
                    val subtotal = orderObj.optDouble("subtotal", 27.95)
                    "$${"%.2f".format(subtotal)}"
                } catch (e: Exception) {
                    "$27.95"
                }
            } else "$27.95"
        }
        
        "{{TAX}}", "{{TAX_AMOUNT}}" -> {
            if (order != null) {
                try {
                    val orderObj = order as JSONObject
                    val taxAmount = orderObj.optDouble("taxAmount", 2.24)
                    "$${"%.2f".format(taxAmount)}"
                } catch (e: Exception) {
                    "$2.24"
                }
            } else "$2.24"
        }
        
        "{{TAX_RATE}}" -> {
            if (order != null) {
                try {
                    val orderObj = order as JSONObject
                    val taxRate = orderObj.optDouble("taxRate", 0.08)
                    "${"%.1f".format(taxRate * 100)}%"
                } catch (e: Exception) {
                    "8.0%"
                }
            } else "8.0%"
        }
        
        "{{TOTAL}}" -> {
            if (order != null) {
                try {
                    val orderObj = order as JSONObject
                    val total = orderObj.optDouble("totalAmount", 30.19)
                    "$${"%.2f".format(total)}"
                } catch (e: Exception) {
                    "$30.19"
                }
            } else "$30.19"
        }
        
        // Item information
        "{{ITEM_LIST}}" -> {
            if (order != null) {
                try {
                    val orderObj = order as JSONObject
                    val items = orderObj.optJSONArray("items")
                    if (items != null && items.length() > 0) {
                        val itemList = StringBuilder()
                        
                        for (i in 0 until items.length()) {
                            val item = items.getJSONObject(i)
                            val name = item.optString("name", "Unknown Item")
                            val quantity = item.optInt("quantity", 1)
                            val totalPrice = item.optDouble("totalPrice", 0.0)
                            val unitPrice = item.optDouble("unitPrice", 0.0)
                            
                            itemList.append("${name.padEnd(32)} x$quantity ${"$%.2f".format(totalPrice)}")
                            if (quantity > 1 && unitPrice > 0) {
                                itemList.append("\n  @ ${"$%.2f".format(unitPrice)} each")
                            }
                            if (i < items.length() - 1) {
                                itemList.append("\n")
                            }
                        }
                        
                        itemList.toString()
                    } else {
                        "No items"
                    }
                } catch (e: Exception) {
                    "Error loading items"
                }
            } else {
                "Cheeseburger                    x2      $17.98\n  @ $8.99 each\nFrench Fries                    x1       $3.99\nSoft Drink                      x2       $5.98\n  @ $2.99 each"
            }
        }
        
        "{{ITEM_COUNT}}" -> {
            if (order != null) {
                try {
                    val orderObj = order as JSONObject
                    val items = orderObj.optJSONArray("items")
                    if (items != null) {
                        var totalQuantity = 0
                        for (i in 0 until items.length()) {
                            val item = items.getJSONObject(i)
                            totalQuantity += item.optInt("quantity", 1)
                        }
                        totalQuantity.toString()
                    } else {
                        "0"
                    }
                } catch (e: Exception) {
                    "0"
                }
            } else "3"
        }
        
        // Customer information
        "{{CUSTOMER_NAME}}" -> {
            if (order != null) {
                try {
                    val orderObj = order as JSONObject
                    val customerInfo = orderObj.optJSONObject("customerInfo")
                    customerInfo?.optString("name", "Guest") ?: "Guest"
                } catch (e: Exception) {
                    "Guest"
                }
            } else "Guest"
        }
        
        "{{CUSTOMER_PHONE}}" -> "N/A"
        
        "{{CUSTOMER_ID}}" -> {
            if (order != null) {
                try {
                    val orderObj = order as JSONObject
                    val customerInfo = orderObj.optJSONObject("customerInfo")
                    customerInfo?.optString("customerId", "N/A") ?: "N/A"
                } catch (e: Exception) {
                    "N/A"
                }
            } else "N/A"
        }
        
        "{{MEMBER_STATUS}}" -> {
            if (order != null) {
                try {
                    val orderObj = order as JSONObject
                    val customerInfo = orderObj.optJSONObject("customerInfo")
                    customerInfo?.optString("memberStatus", "REGULAR") ?: "REGULAR"
                } catch (e: Exception) {
                    "REGULAR"
                }
            } else "REGULAR"
        }
        
        "{{LOYALTY_POINTS}}" -> {
            if (order != null) {
                try {
                    val orderObj = order as JSONObject
                    val customerInfo = orderObj.optJSONObject("customerInfo")
                    customerInfo?.optInt("loyaltyPoints", 0)?.toString() ?: "0"
                } catch (e: Exception) {
                    "0"
                }
            } else "0"
        }
        
        // Payment information
        "{{PAYMENT_METHOD}}" -> {
            if (order != null) {
                try {
                    val orderObj = order as JSONObject
                    orderObj.optString("paymentMethod", "Credit Card")
                } catch (e: Exception) {
                    "Credit Card"
                }
            } else "Credit Card"
        }
        
        "{{CHANGE_DUE}}" -> "$0.00"
        
        // Promotion information
        "{{DISCOUNT}}" -> {
            if (order != null) {
                try {
                    val orderObj = order as JSONObject
                    val orderPromotions = orderObj.optJSONArray("orderPromotions")
                    val itemPromotions = orderObj.optJSONArray("itemPromotions")
                    
                    var totalDiscount = 0.0
                    
                    orderPromotions?.let {
                        for (i in 0 until it.length()) {
                            val promo = it.getJSONObject(i)
                            totalDiscount += promo.optDouble("discountAmount", 0.0)
                        }
                    }
                    
                    itemPromotions?.let {
                        for (i in 0 until it.length()) {
                            val promo = it.getJSONObject(i)
                            totalDiscount += promo.optDouble("discountAmount", 0.0)
                        }
                    }
                    
                    "$${"%.2f".format(totalDiscount)}"
                } catch (e: Exception) {
                    "$0.00"
                }
            } else "$0.00"
        }
        
        "{{PROMOTION_CODE}}" -> ""
        "{{BARCODE_DATA}}" -> "ORD${(10000..99999).random()}"
        "{{QR_DATA}}" -> "https://receipt.example.com/order/${(1000..9999).random()}"
        
        // Lowercase variants for compatibility
        "{{order_number}}" -> {
            if (order != null) {
                try {
                    val orderObj = order as JSONObject
                    orderObj.optString("orderId", "A-0042")
                } catch (e: Exception) {
                    "A-0042"
                }
            } else "A-0042"
        }
        
        "{{item_list}}" -> {
            if (order != null) {
                try {
                    val orderObj = order as JSONObject
                    val items = orderObj.optJSONArray("items")
                    if (items != null && items.length() > 0) {
                        val itemList = StringBuilder()
                        
                        for (i in 0 until items.length()) {
                            val item = items.getJSONObject(i)
                            val name = item.optString("name", "Unknown Item")
                            val quantity = item.optInt("quantity", 1)
                            val totalPrice = item.optDouble("totalPrice", 0.0)
                            val unitPrice = item.optDouble("unitPrice", 0.0)
                            
                            itemList.append("${name.padEnd(32)} x$quantity ${"$%.2f".format(totalPrice)}")
                            if (quantity > 1 && unitPrice > 0) {
                                itemList.append("\\n  @ ${"$%.2f".format(unitPrice)} each")
                            }
                            if (i < items.length() - 1) {
                                itemList.append("\\n")
                            }
                        }
                        
                        itemList.toString()
                    } else {
                        "No items"
                    }
                } catch (e: Exception) {
                    "Error loading items"
                }
            } else {
                "Cheeseburger                    x2      $17.98\\n  @ $8.99 each\\nFrench Fries                    x1       $3.99\\nSoft Drink                      x2       $5.98\\n  @ $2.99 each"
            }
        }
        
        // If unknown field, return as-is
        else -> field
    }
}
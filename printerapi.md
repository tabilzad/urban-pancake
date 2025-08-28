#🖨️ EpsonPrinter API Reference

Complete reference for all methods and constants available in your interpreter function.

## Core Printing Methods

```
printer.addText(text: string, style?: TextStyle)
// Prints text with optional styling

// Examples
printer.addText("Hello World");
printer.addText("Bold Text", { bold: true });
printer.addText("Large Text", { bold: true, underline: true, size: 'LARGE' });
```

```
printer.addTextAlign(alignment: Alignment)
Sets text alignment for subsequent text

// Alignment enum: LEFT, CENTER, RIGHT
printer.addTextAlign('CENTER');
printer.addText("Centered Text");
printer.addTextAlign('LEFT'); // Reset to left
```

```
printer.addTextStyle(style: TextStyle)
Sets text style for subsequent text

printer.addTextStyle({ bold: true, underline: false, size: 'LARGE' });
printer.addText("Styled Text");
printer.addTextStyle({ size: 'NORMAL' }); // Reset
```

```
printer.addFeedLine(lines: number)
Feeds blank lines (adds vertical spacing)

printer.addFeedLine(2); // Add 2 blank lines
```

```
printer.cutPaper()
Cuts the paper (marks end of receipt)

printer.addFeedLine(3);
printer.cutPaper(); // Always call at the end
```

## Advanced Methods

```
printer.addQRCode(data: string, options?: QRCodeOptions)
Prints a QR code

printer.addQRCode("https://example.com");
printer.addQRCode("WIFI:T:WPA;S:MyNetwork;P:MyPassword;;", {
  size: 3, // Size multiplier (1-16)
  errorCorrection: 'H' // L, M, Q, or H
});
```

```
printer.addBarcode(data: string, type: BarcodeType, options?: BarcodeOptions)
Prints various types of barcodes

// BarcodeType: CODE128, CODE39, EAN13, UPC_A, etc.
printer.addBarcode("123456789", "CODE128");
printer.addBarcode("ABC123", "CODE39", {
  width: 'MEDIUM', // THIN, MEDIUM, THICK
  height: 50,
  hri: true // Human Readable Interpretation
});
```

```
printer.addTextStyle(style: TextStyle)
Sets default text style for subsequent text

printer.addTextStyle({ bold: true, underline: true });
printer.addText("This will be bold and underlined");
printer.addTextStyle({}); // Reset styles
```

```
printer.addLineSpace(space: number)
Sets line spacing in pixels (default: 30)

printer.addLineSpace(50); // Increase line spacing
printer.addText("Spaced text");
```

```
printer.clear()
Clears the canvas (preview only)

printer.clear(); // Start fresh
```

## Type Definitions & Options

```
TextStyle
interface TextStyle {
  bold?: boolean;
  underline?: boolean;
  size?: TextSize; // SMALL, NORMAL, LARGE, XLARGE
}

enum TextSize {
  SMALL = 'SMALL',
  NORMAL = 'NORMAL',
  LARGE = 'LARGE',
  XLARGE = 'XLARGE'
}
```

```
BarcodeType
enum BarcodeType {
  UPC_A, UPC_E, EAN13, EAN8,
  CODE39, ITF, CODABAR, CODE93, CODE128,
  GS1_128, GS1_DATABAR_OMNIDIRECTIONAL,
  GS1_DATABAR_TRUNCATED, GS1_DATABAR_LIMITED,
  GS1_DATABAR_EXPANDED
}
```

```
QRCodeOptions
interface QRCodeOptions {
  size?: number;  // Size multiplier (default: 3)
  errorCorrection?: QRErrorCorrection; // L, M, Q, H (default: M)
}

enum QRErrorCorrection {
  L = 'L', M = 'M', Q = 'Q', H = 'H'
}
```

```
BarcodeOptions & Alignment
interface BarcodeOptions {
  width?: BarcodeWidth;  // THIN, MEDIUM, THICK (default: MEDIUM)
  height?: number;  // Height in pixels (default: 50)
  hri?: boolean;  // Human Readable Interpretation (default: true)
}

enum Alignment {
  LEFT = 'LEFT',
  CENTER = 'CENTER',
  RIGHT = 'RIGHT'
}
```

---

## Complete Example 

```
function interpret(jsonString, printer, order) {
  const json = JSON.parse(jsonString);
  
  // Header
  printer.addTextAlign('CENTER');
  printer.addText(json.storeName || "STORE NAME", { 
    bold: true, 
    size: 'XLARGE' 
  });
  printer.addFeedLine(1);
  
  // Date/Time
  printer.addText(new Date().toLocaleString());
  printer.addFeedLine(2);
  
  // Items (if order provided)
  if (order) {
    printer.addTextAlign('LEFT');
    printer.addText("ORDER #" + order.orderId);
    printer.addFeedLine(1);
    
    order.items.forEach(item => {
      const line = item.name.padEnd(20) + " $" + item.totalPrice.toFixed(2);
      printer.addText(line);
    });
    
    printer.addFeedLine(1);
    printer.addTextAlign('RIGHT');
    printer.addText("TOTAL: $" + order.totalAmount.toFixed(2), { 
      bold: true,
      size: 'LARGE'
    });
  }
  
  // QR Code
  printer.addFeedLine(2);
  printer.addTextAlign('CENTER');
  printer.addQRCode("https://payment.example.com/order/" + (order?.orderId || "test"), {
    size: 3,
    errorCorrection: 'H'
  });
  
  // Footer
  printer.addFeedLine(1);
  printer.addText("Thank you for your business!");
  printer.addFeedLine(3);
  printer.cutPaper();
}
```

## Pro Tips

> 💡 Pro Tips
> Always call printer.cutPaper() at the end
> 
> Reset text size with printer.addTextSize(1, 1) after making text larger
> 
> Use printer.addFeedLine() for spacing instead of adding empty text
> 
> Text alignment affects all subsequent text until changed
> 
> For Kotlin: Replace printer. with printer. (same API)
> 
> In Kotlin, use string templates: "Total: $${amount}"
> 

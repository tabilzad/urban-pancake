```
interface Order {
  // Basic fields (Round 1+)
  orderId: string           // "A-0042"
  storeNumber: string       // "001"
  storeName: string         // "BYTE BURGERS"
  timestamp: number         // Unix timestamp
  
  items: OrderItem[]        // Array of items
  subtotal: number          // 27.95
  taxRate: number           // 0.08
  taxAmount: number         // 2.24
  totalAmount: number       // 30.19
  
  // Round 2+ Promotions
  itemPromotions?: ItemPromotion[]
  orderPromotions?: OrderPromotion[]
  
  // Round 3+ Customer
  customerInfo?: CustomerInfo
  paymentMethod?: string    // "VISA ****1234"
  
  // Round 5+ Split Payment
  splitPayments?: SplitPayment[]
  tableInfo?: TableInfo
}

interface OrderItem {
  name: string              // "Cheeseburger"
  quantity: number          // 2
  unitPrice: number         // 8.99
  totalPrice: number        // 17.98
  sku?: string              // "BURG-001"
  category?: string         // "BURGERS"
  modifiers?: string[]      // ["Medium Rare", "Extra Cheese"]
}

interface CustomerInfo {
  customerId: string
  name: string
  memberStatus?: string     // "GOLD", "PLATINUM"
  loyaltyPoints?: number
  memberSince?: string
}
```

'use client';

import React, { useState, useEffect, useCallback } from 'react';

// TypeScript interfaces based on JSON DSL spec
interface TextStyle {
  bold?: boolean;
  underline?: boolean;
  size?: 'SMALL' | 'NORMAL' | 'LARGE' | 'XLARGE';
}

interface ReceiptElement {
  id: string;
  type: string;
  [key: string]: any;
}

interface TextElement extends ReceiptElement {
  type: 'text';
  content: string;
  style?: TextStyle;
}

interface BarcodeElement extends ReceiptElement {
  type: 'barcode';
  data: string;
  barcodeType: 'CODE39' | 'CODE128' | 'EAN13' | 'UPC_A';
}

interface QRCodeElement extends ReceiptElement {
  type: 'qrcode';
  data: string;
}

interface DividerElement extends ReceiptElement {
  type: 'divider';
}

interface DynamicElement extends ReceiptElement {
  type: 'dynamic';
  field: string;
}

interface FeedElement extends ReceiptElement {
  type: 'feed';
  lines: number;
}

interface ImageElement extends ReceiptElement {
  type: 'image';
  imageData: string;
}

type AnyReceiptElement = TextElement | BarcodeElement | QRCodeElement | DividerElement | DynamicElement | FeedElement | ImageElement;

interface ReceiptDesignerProps {
  onJsonUpdate: (json: string) => void;
}

// Element palette configuration
const ELEMENT_TYPES = [
  { type: 'text', icon: '📝', label: 'Text' },
  { type: 'barcode', icon: '📊', label: 'Barcode' },
  { type: 'qrcode', icon: '🔲', label: 'QR Code' },
  { type: 'divider', icon: '➖', label: 'Divider' },
  { type: 'dynamic', icon: '🔄', label: 'Dynamic Field' },
  { type: 'feed', icon: '📏', label: 'Line Feed' },
  { type: 'image', icon: '🖼️', label: 'Image' },
];

const DYNAMIC_FIELDS = [
  '{store_name}',
  '{store_address}',
  '{cashier_name}',
  '{timestamp}',
  '{order_number}',
  '{subtotal}',
  '{tax}',
  '{total}',
  '{item_list}',
];

export const ReceiptDesigner: React.FC<ReceiptDesignerProps> = ({ onJsonUpdate }) => {
  const [elements, setElements] = useState<AnyReceiptElement[]>([]);
  const [selectedElement, setSelectedElement] = useState<string | null>(null);
  const [draggedType, setDraggedType] = useState<string | null>(null);
  const [nextId, setNextId] = useState(1);
  const [showJsonPanel, setShowJsonPanel] = useState(false);
  const [generatedJson, setGeneratedJson] = useState('');

  // Generate unique ID for new elements
  const generateId = useCallback(() => {
    const id = `element_${nextId}`;
    setNextId(prev => prev + 1);
    return id;
  }, [nextId]);

  // Create default element based on type
  const createDefaultElement = useCallback((type: string): AnyReceiptElement => {
    const id = generateId();
    
    switch (type) {
      case 'text':
        return { 
          id, 
          type: 'text', 
          content: 'Sample Text',
          style: { bold: false, size: 'NORMAL' }
        };
      case 'barcode':
        return { 
          id, 
          type: 'barcode', 
          data: '123456789',
          barcodeType: 'CODE128'
        };
      case 'qrcode':
        return { 
          id, 
          type: 'qrcode', 
          data: 'https://example.com'
        };
      case 'divider':
        return { id, type: 'divider' };
      case 'dynamic':
        return { 
          id, 
          type: 'dynamic', 
          field: '{store_name}'
        };
      case 'feed':
        return { 
          id, 
          type: 'feed', 
          lines: 1
        };
      case 'image':
        return { 
          id, 
          type: 'image', 
          imageData: ''
        };
      default:
        return { 
          id, 
          type: 'text', 
          content: 'Unknown Element'
        };
    }
  }, [generateId]);

  // Update JSON whenever elements change
  useEffect(() => {
    const jsonOutput = {
      elements: elements.map(element => {
        const { id, ...elementWithoutId } = element;
        return elementWithoutId;
      })
    };
    const jsonString = JSON.stringify(jsonOutput, null, 2);
    setGeneratedJson(jsonString);
    onJsonUpdate(jsonString);
  }, [elements, onJsonUpdate]);

  // Handle drag start from palette
  const handleDragStart = (e: React.DragEvent, elementType: string) => {
    setDraggedType(elementType);
    e.dataTransfer.effectAllowed = 'copy';
  };

  // Handle drop on canvas
  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    if (draggedType) {
      const newElement = createDefaultElement(draggedType);
      setElements(prev => [...prev, newElement]);
      setDraggedType(null);
    }
  };

  // Handle drag over canvas
  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    e.dataTransfer.dropEffect = 'copy';
  };

  // Delete element
  const deleteElement = (id: string) => {
    setElements(prev => prev.filter(el => el.id !== id));
    if (selectedElement === id) {
      setSelectedElement(null);
    }
  };

  // Move element up
  const moveElementUp = (id: string) => {
    setElements(prev => {
      const index = prev.findIndex(el => el.id === id);
      if (index > 0) {
        const newElements = [...prev];
        [newElements[index - 1], newElements[index]] = [newElements[index], newElements[index - 1]];
        return newElements;
      }
      return prev;
    });
  };

  // Move element down
  const moveElementDown = (id: string) => {
    setElements(prev => {
      const index = prev.findIndex(el => el.id === id);
      if (index < prev.length - 1) {
        const newElements = [...prev];
        [newElements[index], newElements[index + 1]] = [newElements[index + 1], newElements[index]];
        return newElements;
      }
      return prev;
    });
  };

  // Update element property
  const updateElement = (id: string, updates: Partial<AnyReceiptElement>) => {
    setElements(prev => prev.map(el => el.id === id ? { ...el, ...updates } : el));
  };

  // Get selected element
  const getSelectedElement = () => {
    return elements.find(el => el.id === selectedElement);
  };

  // Copy JSON to clipboard
  const copyJsonToClipboard = async () => {
    try {
      await navigator.clipboard.writeText(generatedJson);
      // You could add a toast notification here
    } catch (err) {
      console.error('Failed to copy JSON:', err);
    }
  };

  // Initialize with sample receipt
  useEffect(() => {
    const sampleElements: AnyReceiptElement[] = [
      { id: 'sample_1', type: 'text', content: 'Welcome to {{STORE_NAME}}', style: { bold: true, size: 'LARGE' } },
      { id: 'sample_2', type: 'text', content: 'Store #{{STORE_NUMBER}}', style: { size: 'NORMAL' } },
      { id: 'sample_3', type: 'feed', lines: 1 },
      { id: 'sample_4', type: 'dynamic', field: '{order_number}' },
      { id: 'sample_5', type: 'feed', lines: 1 },
      { id: 'sample_6', type: 'divider' },
      { id: 'sample_7', type: 'dynamic', field: '{item_list}' },
      { id: 'sample_8', type: 'divider' },
      { id: 'sample_9', type: 'text', content: 'Thank you for your order!', style: { size: 'NORMAL' } },
      { id: 'sample_10', type: 'feed', lines: 3 }
    ];
    setElements(sampleElements);
    setNextId(11);
  }, []);

  return (
    <div className="h-full flex bg-gray-900 relative">
      {/* Element Palette */}
      <div className="w-64 bg-gray-800 border-r border-gray-700 p-4">
        <h3 className="text-lg font-bold mb-4 text-gray-100">Element Palette</h3>
        <div className="space-y-2">
          {ELEMENT_TYPES.map(elementType => (
            <div
              key={elementType.type}
              draggable
              onDragStart={(e) => handleDragStart(e, elementType.type)}
              className="flex items-center gap-3 p-3 bg-gray-700 rounded-lg cursor-grab hover:bg-gray-600 transition-colors"
            >
              <span className="text-xl">{elementType.icon}</span>
              <span className="text-gray-200">{elementType.label}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Main Canvas */}
      <div className="flex-1 flex flex-col">
        <div className="bg-gray-800 border-b border-gray-700 p-4 flex justify-between items-center">
          <div>
            <h2 className="text-xl font-bold text-gray-100">Receipt Designer</h2>
            <p className="text-gray-400">Drag elements from the palette to design your receipt</p>
          </div>
          
          {/* JSON Panel Toggle Button */}
          <button
            onClick={() => setShowJsonPanel(!showJsonPanel)}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg font-medium transition-all ${
              showJsonPanel 
                ? 'bg-blue-600 text-white' 
                : 'bg-gray-700 text-gray-300 hover:bg-gray-600'
            }`}
          >
            <span>📄</span>
            <span>JSON</span>
          </button>
        </div>
        
        <div className="flex-1 p-6 overflow-auto">
          <div
            onDrop={handleDrop}
            onDragOver={handleDragOver}
            className="min-h-96 bg-white rounded-lg shadow-lg p-6 border-2 border-dashed border-gray-300 hover:border-blue-400 transition-colors"
          >
            {elements.length === 0 ? (
              <div className="text-center text-gray-500 py-20">
                <div className="text-6xl mb-4">📋</div>
                <p className="text-lg">Drop elements here to start designing</p>
              </div>
            ) : (
              <div className="space-y-2">
                {elements.map((element, index) => (
                  <div
                    key={element.id}
                    className={`group flex items-center gap-2 p-3 rounded-lg border-2 transition-colors ${
                      selectedElement === element.id
                        ? 'border-blue-500 bg-blue-50'
                        : 'border-gray-200 hover:border-gray-300'
                    }`}
                    onClick={() => setSelectedElement(element.id)}
                  >
                    <span className="text-sm font-mono text-gray-500 w-8">{index + 1}</span>
                    <div className="flex-1">
                      <ElementPreview element={element} />
                    </div>
                    <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                      <button
                        onClick={(e) => { e.stopPropagation(); moveElementUp(element.id); }}
                        disabled={index === 0}
                        className="p-1 text-gray-400 hover:text-gray-600 disabled:opacity-50"
                        title="Move up"
                      >
                        ⬆️
                      </button>
                      <button
                        onClick={(e) => { e.stopPropagation(); moveElementDown(element.id); }}
                        disabled={index === elements.length - 1}
                        className="p-1 text-gray-400 hover:text-gray-600 disabled:opacity-50"
                        title="Move down"
                      >
                        ⬇️
                      </button>
                      <button
                        onClick={(e) => { e.stopPropagation(); deleteElement(element.id); }}
                        className="p-1 text-red-400 hover:text-red-600"
                        title="Delete"
                      >
                        🗑️
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Properties Panel */}
      <div className="w-80 bg-gray-800 border-l border-gray-700 p-4">
        <h3 className="text-lg font-bold mb-4 text-gray-100">Properties</h3>
        {selectedElement ? (
          <ElementProperties
            element={getSelectedElement()!}
            onUpdate={(updates) => updateElement(selectedElement, updates)}
          />
        ) : (
          <div className="text-gray-400 text-center py-8">
            <div className="text-4xl mb-2">⚙️</div>
            <p>Select an element to edit its properties</p>
          </div>
        )}
      </div>

      {/* JSON Panel - Sliding from right */}
      <div className={`fixed top-0 right-0 h-full w-96 bg-gray-800 border-l border-gray-700 transform transition-transform duration-300 ease-in-out z-50 ${
        showJsonPanel ? 'translate-x-0' : 'translate-x-full'
      }`}>
        <div className="flex flex-col h-full">
          {/* JSON Panel Header */}
          <div className="bg-gray-700 p-4 border-b border-gray-600 flex justify-between items-center">
            <h3 className="text-lg font-bold text-gray-100">Generated JSON</h3>
            <div className="flex gap-2">
              <button
                onClick={copyJsonToClipboard}
                className="px-3 py-1 bg-green-600 text-white rounded hover:bg-green-700 transition-colors text-sm"
                title="Copy to clipboard"
              >
                📋 Copy
              </button>
              <button
                onClick={() => setShowJsonPanel(false)}
                className="px-3 py-1 bg-red-600 text-white rounded hover:bg-red-700 transition-colors text-sm"
                title="Close panel"
              >
                ✕
              </button>
            </div>
          </div>
          
          {/* JSON Content */}
          <div className="flex-1 overflow-auto p-4">
            <pre className="bg-gray-900 text-green-400 p-4 rounded-lg text-sm overflow-auto whitespace-pre-wrap font-mono border border-gray-600">
              {generatedJson || '{\n  "elements": []\n}'}
            </pre>
          </div>
          
          {/* JSON Panel Footer */}
          <div className="bg-gray-700 p-3 border-t border-gray-600">
            <div className="text-xs text-gray-400">
              <p>Elements: {elements.length}</p>
              <p>JSON Size: {new Blob([generatedJson]).size} bytes</p>
            </div>
          </div>
        </div>
      </div>

      {/* Overlay when JSON panel is open */}
      {showJsonPanel && (
        <div 
          className="fixed inset-0 bg-black bg-opacity-50 z-40"
          onClick={() => setShowJsonPanel(false)}
        />
      )}
    </div>
  );
};

// Component to preview elements in the canvas
const ElementPreview: React.FC<{ element: AnyReceiptElement }> = ({ element }) => {
  switch (element.type) {
    case 'text':
      return (
        <div className={`${element.style?.bold ? 'font-bold' : ''} ${
          element.style?.size === 'SMALL' ? 'text-sm' : 
          element.style?.size === 'LARGE' ? 'text-lg' : 
          element.style?.size === 'XLARGE' ? 'text-xl' : ''
        }`}>
          📝 {element.content}
        </div>
      );
    case 'barcode':
      return <div className="text-gray-600">📊 Barcode: {element.data} ({element.barcodeType})</div>;
    case 'qrcode':
      return <div className="text-gray-600">🔲 QR Code: {element.data}</div>;
    case 'divider':
      return <div className="text-gray-600">➖ Divider Line</div>;
    case 'dynamic':
      return <div className="text-gray-600">🔄 Dynamic: {element.field}</div>;
    case 'feed':
      return <div className="text-gray-600">📏 Feed {element.lines} line(s)</div>;
    case 'image':
      return <div className="text-gray-600">🖼️ Image {element.imageData ? '(with data)' : '(no data)'}</div>;
    default:
      return <div className="text-gray-400">Unknown element</div>;
  }
};

// Properties panel for editing selected element
const ElementProperties: React.FC<{
  element: AnyReceiptElement;
  onUpdate: (updates: Partial<AnyReceiptElement>) => void;
}> = ({ element, onUpdate }) => {
  const renderProperties = () => {
    switch (element.type) {
      case 'text':
        return (
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">Content</label>
              <textarea
                value={element.content}
                onChange={(e) => onUpdate({ content: e.target.value })}
                className="w-full px-3 py-2 bg-gray-700 text-white rounded-lg border border-gray-600 focus:border-blue-500 focus:outline-none"
                rows={3}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">Size</label>
              <select
                value={element.style?.size || 'NORMAL'}
                onChange={(e) => onUpdate({ style: { ...element.style, size: e.target.value as any } })}
                className="w-full px-3 py-2 bg-gray-700 text-white rounded-lg border border-gray-600 focus:border-blue-500 focus:outline-none"
              >
                <option value="SMALL">Small</option>
                <option value="NORMAL">Normal</option>
                <option value="LARGE">Large</option>
                <option value="XLARGE">X-Large</option>
              </select>
            </div>
            <div className="flex gap-4">
              <label className="flex items-center gap-2 text-gray-300">
                <input
                  type="checkbox"
                  checked={element.style?.bold || false}
                  onChange={(e) => onUpdate({ style: { ...element.style, bold: e.target.checked } })}
                  className="rounded"
                />
                Bold
              </label>
              <label className="flex items-center gap-2 text-gray-300">
                <input
                  type="checkbox"
                  checked={element.style?.underline || false}
                  onChange={(e) => onUpdate({ style: { ...element.style, underline: e.target.checked } })}
                  className="rounded"
                />
                Underline
              </label>
            </div>
          </div>
        );

      case 'barcode':
        return (
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">Barcode Data</label>
              <input
                type="text"
                value={element.data}
                onChange={(e) => onUpdate({ data: e.target.value })}
                className="w-full px-3 py-2 bg-gray-700 text-white rounded-lg border border-gray-600 focus:border-blue-500 focus:outline-none"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">Barcode Type</label>
              <select
                value={element.barcodeType}
                onChange={(e) => onUpdate({ barcodeType: e.target.value })}
                className="w-full px-3 py-2 bg-gray-700 text-white rounded-lg border border-gray-600 focus:border-blue-500 focus:outline-none"
              >
                <option value="CODE39">CODE39</option>
                <option value="CODE128">CODE128</option>
                <option value="EAN13">EAN13</option>
                <option value="UPC_A">UPC_A</option>
              </select>
            </div>
          </div>
        );

      case 'qrcode':
        return (
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">QR Code Data</label>
              <textarea
                value={element.data}
                onChange={(e) => onUpdate({ data: e.target.value })}
                className="w-full px-3 py-2 bg-gray-700 text-white rounded-lg border border-gray-600 focus:border-blue-500 focus:outline-none"
                rows={3}
              />
            </div>
          </div>
        );

      case 'dynamic':
        return (
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">Dynamic Field</label>
              <select
                value={element.field}
                onChange={(e) => onUpdate({ field: e.target.value })}
                className="w-full px-3 py-2 bg-gray-700 text-white rounded-lg border border-gray-600 focus:border-blue-500 focus:outline-none"
              >
                {DYNAMIC_FIELDS.map(field => (
                  <option key={field} value={field}>{field}</option>
                ))}
              </select>
            </div>
          </div>
        );

      case 'feed':
        return (
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">Number of Lines</label>
              <input
                type="number"
                min="1"
                max="10"
                value={element.lines}
                onChange={(e) => onUpdate({ lines: parseInt(e.target.value) || 1 })}
                className="w-full px-3 py-2 bg-gray-700 text-white rounded-lg border border-gray-600 focus:border-blue-500 focus:outline-none"
              />
            </div>
          </div>
        );

      case 'image':
        return (
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">Image Data (Base64)</label>
              <textarea
                value={element.imageData}
                onChange={(e) => onUpdate({ imageData: e.target.value })}
                placeholder="Paste base64 encoded image data here..."
                className="w-full px-3 py-2 bg-gray-700 text-white rounded-lg border border-gray-600 focus:border-blue-500 focus:outline-none"
                rows={4}
              />
            </div>
          </div>
        );

      default:
        return <div className="text-gray-400">No properties available for this element type.</div>;
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-gray-700 p-3 rounded-lg">
        <h4 className="font-medium text-gray-200 mb-1">Element Type</h4>
        <p className="text-gray-400 text-sm">{element.type.charAt(0).toUpperCase() + element.type.slice(1)}</p>
      </div>
      
      {renderProperties()}
      
      <div className="bg-gray-700 p-3 rounded-lg">
        <h4 className="font-medium text-gray-200 mb-1">Element ID</h4>
        <p className="text-gray-400 text-sm font-mono">{element.id}</p>
      </div>
    </div>
  );
};
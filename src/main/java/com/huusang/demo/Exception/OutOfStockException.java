package com.huusang.demo.Exception;

/**
 * Ném ra khi tồn kho không đủ trong quá trình checkout (pessimistic locking).
 * Khi exception này được throw, @Transactional sẽ rollback toàn bộ transaction
 * và nhả tất cả các pessimistic lock đã giữ.
 */
public class OutOfStockException extends RuntimeException {

    private final String productName;
    private final String variantName;
    private final int requestedQuantity;
    private final int availableQuantity;

    public OutOfStockException(String productName, String variantName,
                               int requestedQuantity, int availableQuantity) {
        super(String.format("Sản phẩm '%s - %s' không đủ hàng. Yêu cầu: %d, còn lại: %d",
                productName, variantName, requestedQuantity, availableQuantity));
        this.productName = productName;
        this.variantName = variantName;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public String getProductName() { return productName; }
    public String getVariantName() { return variantName; }
    public int getRequestedQuantity() { return requestedQuantity; }
    public int getAvailableQuantity() { return availableQuantity; }
}

package no.susoft.mobile.pos.data;

import java.io.Serializable;
import java.util.ArrayList;

import no.susoft.mobile.pos.json.JSONSerializable;

/**
 * This class holds basic information about a product.
 */
public class Product implements JSONSerializable, Serializable {

    // The product id.
    private String id;
    // The product name.
    private String name;
    // The product description
    private String description;
    // The product type
    private String type;
    // The product wholesale cost from vendor.
    private Decimal cost;
    // The product retail price at shop.
    private Decimal price;
    // The product stock count.
    private Decimal stockQty;
    // The product barcode.
    private String barcode;
    // The product discount.
    private Discount discount;
    // The product requires manual price entry.
    private boolean miscellaneous;
    // The product ABC code.
    private String abcCode;
    // The product VAT
    private double vat;
	// The product weight
	private Decimal weight;
	private String categoryId ;
	private String categoryName;
	private boolean useAlternative;
	private Decimal alternativePrice;
	private double alternativeVat;
	private int tare;
	private ArrayList<Product> components;

	public Product() {
        this.setId(null);
        this.setName(null);
        this.setDescription(null);
        this.setCost(null);
        this.setPrice(null);
        this.setStockQty(null);
        this.setBarcode(null);
        this.setDiscount(null);
        this.setVat(0);
    }

    public Product(Product oldProduct) {
        this.setId(oldProduct.getId());
        this.setName(oldProduct.getName());
        this.setDescription(oldProduct.getDescription());
        this.setCost(oldProduct.getCost());
        this.setPrice(oldProduct.getPrice());
        this.setStockQty(oldProduct.getStockQty());
        this.setBarcode(oldProduct.getBarcode());
        this.setDiscount(oldProduct.getDiscount());
        this.setVat(oldProduct.getVat());
        this.setMiscellaneous(oldProduct.isMiscellaneous());
        this.setType(oldProduct.getType());
        this.setAbcCode(oldProduct.getAbcCode());
        this.setCategoryId(oldProduct.getCategoryId());
        this.setCategoryName(oldProduct.getCategoryName());
        this.setUseAlternative(oldProduct.isUseAlternative());
        this.setAlternativePrice(oldProduct.getAlternativePrice());
        this.setAlternativeVat(oldProduct.getAlternativeVat());
        this.setTare(oldProduct.getTare());
    }

    /**
     * Get the product id.
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * Set the product id.
     *
     * @param id
     */
    public void setId(String id) {
        if (id == null)
            id = "";
        this.id = id.trim();
    }

    /**
     * Get the product name.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Set the product name.
     *
     * @param name
     */
    public void setName(String name) {
        if (name == null)
            name = "";
        this.name = name.trim();
    }

    /**
     * Get the product description.
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the product description.
     *
     * @param description
     */
    public void setDescription(String description) {
        if (description == null)
            description = "";
        this.description = description.trim();
    }

    /**
     * Get the product type.
     *
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * Set the product type.
     *
     * @param type
     */
    public void setType(String type) {
        if (type == null)
            type = "";
        this.type = type.trim();
    }

    /**
     * Get the product cost value.
     *
     * @return
     */
    public Decimal getCost() {
        return cost;
    }

    /**
     * Set the product cost value.
     *
     * @param cost
     */
    public void setCost(Decimal cost) {
        this.cost = Decimal.minZero(cost);
    }

    /**
     * Get the product retail price.
     *
     * @return
     */
    public Decimal getPrice() {
        return price;
    }

    /**
     * Set the product retail price.
     *
     * @param price
     */
    public void setPrice(Decimal price) {
        this.price = Decimal.minZero(price);
    }

    /**
     * Get the product stock count.
     *
     * @return
     */
    public Decimal getStockQty() {
        return stockQty;
    }

    /**
     * Set the product stock count.
     *
     * @param stockQty
     */
    public void setStockQty(Decimal stockQty) {
        this.stockQty = Decimal.validate(stockQty);
    }

    /**
     * Get the product barcode.
     *
     * @return
     */
    public String getBarcode() {
        return barcode;
    }

    /**
     * Set the product barcode.
     *
     * @param barcode
     */
    public void setBarcode(String barcode) {
        if (barcode == null)
            barcode = "";
        this.barcode = barcode.trim();
    }

    /**
     * Get the product discount.
     *
     * @return
     */
    public Discount getDiscount() {
        return this.discount;
    }

    /**
     * Set the product discount.
     *
     * @param discount
     */
    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    /**
     * Return whether this product requires manual pricing.
     *
     * @return
     */
    public boolean isMiscellaneous() {
        return miscellaneous;
    }

    /**
     * Set whether this product requires manual pricing.
     *
     * @param isMiscellaneous
     */
    public void setMiscellaneous(boolean isMiscellaneous) {
        this.miscellaneous = isMiscellaneous;
    }

    public boolean isBundle() {
        return type != null && type.equals("7");
    }

	public String getAbcCode() {
		return abcCode;
	}

	public void setAbcCode(String abcCode) {
		this.abcCode = abcCode;
	}

	public boolean isWeighted() {
		if (abcCode != null && abcCode.equals("W")) {
			return true;
		}
		return false;
	}

	public boolean isBarcodeEANWithWeights() {
		if (barcode != null && barcode.length() == 13 && (barcode.startsWith("20") || barcode.startsWith("21") || barcode.startsWith("22") || barcode.startsWith("23") || barcode.startsWith("24") || barcode.startsWith("25"))) {
			return true;
		}
		return false;
	}

    /**
     * Get the product VAT.
     */
    public double getVat() {
        return vat;
    }

    /**
     * Set the product VAT.
     *
     * @param percent
     */
    public void setVat(double percent) {
        this.vat = percent;
    }

	public Decimal getWeight() {
		return weight;
	}

	public void setWeight(Decimal weight) {
		this.weight = weight;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public boolean isUseAlternative() {
		return useAlternative;
	}

	public void setUseAlternative(boolean useAlternative) {
		this.useAlternative = useAlternative;
	}

	public Decimal getAlternativePrice() {
		return alternativePrice;
	}

	public void setAlternativePrice(Decimal alternativePrice) {
		this.alternativePrice = alternativePrice;
	}

	public double getAlternativeVat() {
		return alternativeVat;
	}

	public void setAlternativeVat(double alternativeVat) {
		this.alternativeVat = alternativeVat;
	}

	public int getTare() {
		return tare;
	}

	public void setTare(int tare) {
		this.tare = tare;
	}

	public ArrayList<Product> getComponents() {
		return components;
	}

	public void setComponents(ArrayList<Product> components) {
		this.components = components;
	}

	/**
     * Get whether this product instance is considered fully resolved.
     *
     * @return
     */
    public boolean isResolved() {
        if (!this.hasID())
            return false;
        if (!this.hasBarcode())
            return false;
        if (!this.hasName())
            return false;
        return true;
    }

    /**
     * Set this product is not resolved.
     */
    public void unResolve() {
        // Just wipe out the id, for now.
        this.setId(null);
    }

    /**
     * Determine whether this item has a non-null, non-invalid barcode.
     *
     * @return
     */
    public boolean isValid() {
        return this.hasBarcode();
    }

    /**
     * Determine whether this item has a non-null, non-invalid barcode.
     *
     * @return
     */
    public boolean hasBarcode() {
        return !this.getBarcode().isEmpty();
    }

    /**
     * Determine whether this item has a non-null, non-invalid id.
     *
     * @return
     */
    public boolean hasID() {
        return !this.getId().isEmpty();
    }

    /**
     * Determine whether this item has a non-null, non-invalid name.
     *
     * @return
     */
    public boolean hasName() {
        return !this.getName().isEmpty();
    }

    /**
     * Return whether this product has a discount attached.
     *
     * @return
     */
    public boolean hasDiscount() {
        return this.discount != null;
    }

    /**
     * Return whether this product has a non-zero, non-negative retail price that was manually set.
     *
     * @return
     */
    public boolean wasManuallyRetailPriced() {
        if (this.isMiscellaneous()) {
            if (this.getPrice().isPositive())
                return true;
        }
        return false;
    }

    /**
     * Determine whether the 'other' product is at least similar to this product.
     * Three criteria is checked:
     * <strike>1) id</strike>,
     * 2) name,
     * 3) barcode.
     * If all are equal, then they are considered similar.
     *
     * @param other
     * @return
     */
    public boolean isSimilar(Product other) {
        if (null == other)
            return false;
        if (this == other)
            return true;
        if (other instanceof Product) {
            final Product p = (Product) other;
            if (this.name != null)
                if (!this.name.equals(p.name))
                    return false;
            if (this.barcode != null)
                if (!this.barcode.equals(p.barcode))
                    return false;
            return true;
        } else {
            return false;
        }
    }
}
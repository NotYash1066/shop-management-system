package com.shopmanagement.rest;

import com.shopmanagement.entity.Supplier;
import com.shopmanagement.repository.SupplierRepository;
import com.shopmanagement.services.CurrentUserService;

import jakarta.transaction.Transactional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@PreAuthorize("hasAuthority('supplier:read')")
@Transactional
public class SupplierController {

	private final SupplierRepository supplierRepository;
    private final com.shopmanagement.repository.ShopRepository shopRepository;
    private final CurrentUserService currentUserService;

    public SupplierController(
            SupplierRepository supplierRepository,
            com.shopmanagement.repository.ShopRepository shopRepository,
            CurrentUserService currentUserService) {
        this.supplierRepository = supplierRepository;
        this.shopRepository = shopRepository;
        this.currentUserService = currentUserService;
    }

	@GetMapping
	public List<Supplier> getAllSuppliers() {
        Long shopId = currentUserService.getCurrentShopId();
			return supplierRepository.findByShopId(shopId);
	}

	@GetMapping("/{id}")
	public Supplier getSupplierById(@PathVariable Long id) {
        Long shopId = currentUserService.getCurrentShopId();
			return supplierRepository.findByIdAndShopId(id, shopId);
	}

	@PostMapping
    @PreAuthorize("hasAuthority('supplier:write')")
	public Supplier createSupplier(@RequestBody Supplier supplier) {
        Long shopId = currentUserService.getCurrentShopId();
        supplier.setShop(shopRepository.getReferenceById(shopId));
			return supplierRepository.save(supplier);
	}

	@PutMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier:write')")
	public Supplier updateSupplier(@PathVariable Long id, @RequestBody Supplier supplier) {
        Long shopId = currentUserService.getCurrentShopId();
        Supplier existing = supplierRepository.findByIdAndShopId(id, shopId);
        if (existing == null) {
             throw new RuntimeException("Supplier not found");
        }
		supplier.setId(id);
        supplier.setShop(shopRepository.getReferenceById(shopId));
		return supplierRepository.save(supplier);
	}

	@DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('supplier:write')")
	public String deleteSupplier(@PathVariable Long id) {
        Long shopId = currentUserService.getCurrentShopId();
        Supplier existing = supplierRepository.findByIdAndShopId(id, shopId);
        if (existing == null) {
             throw new RuntimeException("Supplier not found");
        }
			supplierRepository.deleteById(id);
			return "Supplier deleted";
	}
}

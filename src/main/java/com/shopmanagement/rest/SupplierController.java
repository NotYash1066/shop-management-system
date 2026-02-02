package com.shopmanagement.rest;

import com.shopmanagement.entity.Supplier;
import com.shopmanagement.repository.SupplierRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@PreAuthorize("hasRole('ADMIN')")
@Transactional
public class SupplierController {

	@Autowired
	private SupplierRepository supplierRepository;
    
    @Autowired
    private com.shopmanagement.repository.ShopRepository shopRepository;

	@GetMapping
	public List<Supplier> getAllSuppliers() {
        Long shopId = getCurrentShopId();
		return supplierRepository.findByShopId(shopId);
	}

	@GetMapping("/{id}")
	public Supplier getSupplierById(@PathVariable Long id) {
        Long shopId = getCurrentShopId();
		return supplierRepository.findByIdAndShopId(id, shopId);
				// .orElseThrow(() -> new RuntimeException("Supplier not found"));
	}

	@PostMapping
	public Supplier createSupplier(@RequestBody Supplier supplier) {
        Long shopId = getCurrentShopId();
        supplier.setShop(shopRepository.getReferenceById(shopId));
		return supplierRepository.save(supplier);
	}

	@PutMapping("/{id}")
	public Supplier updateSupplier(@PathVariable Long id, @RequestBody Supplier supplier) {
        Long shopId = getCurrentShopId();
        Supplier existing = supplierRepository.findByIdAndShopId(id, shopId);
        if (existing == null) {
             throw new RuntimeException("Supplier not found");
        }
		supplier.setId(id);
        supplier.setShop(shopRepository.getReferenceById(shopId));
		return supplierRepository.save(supplier);
	}

	@DeleteMapping("/{id}")
	public String deleteSupplier(@PathVariable Long id) {
        Long shopId = getCurrentShopId();
        Supplier existing = supplierRepository.findByIdAndShopId(id, shopId);
        if (existing == null) {
             throw new RuntimeException("Supplier not found");
        }
		supplierRepository.deleteById(id);
		return "Supplier deleted";
	}
    
    private Long getCurrentShopId() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        com.shopmanagement.security.services.UserDetailsImpl userDetails = (com.shopmanagement.security.services.UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getShopId();
    }
}
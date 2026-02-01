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

	@GetMapping
	public List<Supplier> getAllSuppliers() {
		return supplierRepository.findAll();
	}

	@GetMapping("/{id}")
	public Supplier getSupplierById(@PathVariable Long id) {
		return supplierRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Supplier not found"));
	}

	@PostMapping
	public Supplier createSupplier(@RequestBody Supplier supplier) {
		return supplierRepository.save(supplier);
	}

	@PutMapping("/{id}")
	public Supplier updateSupplier(@PathVariable Long id, @RequestBody Supplier supplier) {
		supplier.setId(id);
		return supplierRepository.save(supplier);
	}

	@DeleteMapping("/{id}")
	public String deleteSupplier(@PathVariable Long id) {
		supplierRepository.deleteById(id);
		return "Supplier deleted";
	}
}
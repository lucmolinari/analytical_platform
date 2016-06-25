package com.lucianomolinari.ap.customerapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @RequestMapping(value = "/customer", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomerId> add(@RequestBody final Customer customer) {
        return new ResponseEntity<>(new CustomerId(customerRepository.add(customer)), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/customer/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Customer> findById(@PathVariable("id") final Long id) {
        final Optional<Customer> customer = customerRepository.findById(id);

        if (!customer.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(customer.get(), HttpStatus.OK);
    }

    private static class CustomerId {
        private final Long id;

        public CustomerId(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }
    }

}

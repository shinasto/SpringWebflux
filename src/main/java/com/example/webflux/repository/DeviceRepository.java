package com.example.webflux.repository;

import com.example.webflux.data.Device;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends ReactiveCrudRepository<Device, Long> {
}

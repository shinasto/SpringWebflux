package com.example.webflux.controller;

import com.example.webflux.data.Device;
import com.example.webflux.repository.DeviceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RestController
@RequestMapping(path = "/webflux/devices", produces = "application/json")
@CrossOrigin(origins = "*")
public class DeviceContoller {

    @Autowired
    DeviceRepository deviceRepo;

    @Autowired
    @Qualifier("server1")
    WebClient webClient;

    @GetMapping()
    public Flux<Device> getDevices() {
        return deviceRepo.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Device> getDeviceById(@PathVariable("id") Long id) {
        Mono<Device> device = deviceRepo.findById(id);
        return device;
    }

    @GetMapping("/multi")
    public Flux<Device> getDevice() {
        return Flux
            .range(1, 100)
            .flatMap(i -> {
                return getDeviceFromThirdParty(Long.valueOf(i));
            }).subscribeOn(Schedulers.parallel());
    }

    private Mono<Device> getDeviceFromThirdParty(Long id) {
        return webClient
                .get()
                .uri("/devices/{id}", id)
                .exchange()
                .flatMap(cr -> {
                    log.info("StatusCode={}", cr.statusCode());
                    log.info("Headers={}", cr.headers());
                    return cr.bodyToMono(Device.class);
                });
    }

    @GetMapping("/{id}/third-party")
    public Mono<Device> getDeviceByIdFromThirdParty(@PathVariable("id") Long id) {
        log.info("getDeviceByIdFromThirdParty");

        Mono<Device> device = getDeviceFromThirdParty(id);

        Mono<Device> device2 = webClient
                .post()
                .uri("/devices")
                .body(device, Device.class)
                .exchange()
                .flatMap(cr -> cr.bodyToMono(Device.class));

        return deviceRepo.saveAll(device2).next();
    }

    @PostMapping(consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Device> createDevice(@RequestBody Mono<Device> device) {
        return device.flatMap(i -> deviceRepo.save(i));
    }

    @DeleteMapping("/{id}")
    public void deleteDevice(@PathVariable("id") Long id) {
        deviceRepo.deleteById(id);
    }

}

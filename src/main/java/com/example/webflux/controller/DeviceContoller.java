package com.example.webflux.controller;

import com.example.webflux.data.Device;
import com.example.webflux.repository.DeviceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.cache.CacheMono;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.publisher.Signal;
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

    @Autowired
    CacheManager cacheManager;

    @GetMapping()
    public Flux<Device> getDevices() {
        return deviceRepo.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Device> getDeviceById(@PathVariable("id") Long id) {
        Cache cache = cacheManager.getCache("devices");
        return CacheMono.lookup(key -> {
            //return Mono.<Signal<Device>>justOrEmpty((Signal) cacheManager.getCache("devices").get(key).get());
            Device d = cache.get(key, Device.class);
            return Mono.justOrEmpty(d).map(Signal::next);
        }, id)
                .onCacheMissResume(Mono.defer(() -> deviceRepo.findById(id)))
                .andWriteWith((k, signal) -> Mono.fromRunnable(() -> {
                    if (!signal.isOnError()) {
                        cache.put(k, signal.get());
                    }
                }));
    }

    @DeleteMapping("/{id}")
    public void deleteDevice(@PathVariable("id") Long id) {
        Cache cache = cacheManager.getCache("devices");
        cache.evictIfPresent(id);
        deviceRepo.deleteById(id);
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
        return device.flatMap(i -> {

            return deviceRepo.save(i);
        });
    }

}

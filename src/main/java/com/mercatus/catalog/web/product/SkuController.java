package com.mercatus.catalog.web.product;

import com.mercatus.catalog.application.product.port.in.GetSkuByCodeUseCase;
import com.mercatus.catalog.web.product.dto.SkuLookupResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/skus")
class SkuController {

    private final GetSkuByCodeUseCase getSkuByCode;

    SkuController(GetSkuByCodeUseCase getSkuByCode) {
        this.getSkuByCode = getSkuByCode;
    }

    @GetMapping("/{code}")
    SkuLookupResponse getByCode(@PathVariable String code) {
        return SkuLookupResponse.from(getSkuByCode.getByCode(code));
    }
}

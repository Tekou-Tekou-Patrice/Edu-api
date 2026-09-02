package com.eduguest.Edu.Service;

import com.eduguest.Edu.Entity.SaasSetting;
import com.eduguest.Edu.Repository.SaasSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaasSettingService {
    private final SaasSettingRepository repository;

    public SaasSettingService(SaasSettingRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public SaasSetting get() {
        return repository.findById(1L).orElseGet(() -> repository.save(new SaasSetting()));
    }

    @Transactional
    public SaasSetting save(SaasSetting settings) {
        settings.setId(1L);
        return repository.save(settings);
    }
}

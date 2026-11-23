package mm.health_data_tracker.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.health_data_tracker.model.AppUser;
import mm.health_data_tracker.model.TestParameter;
import mm.health_data_tracker.model.TestResult;
import mm.health_data_tracker.repository.AppUserRepository;
import mm.health_data_tracker.repository.TestParameterRepository;
import mm.health_data_tracker.repository.TestResultRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final TestParameterRepository parameterRepository;
    private final TestResultRepository resultRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            log.info("Demo data seeding is disabled via app.seed.enabled=false");
            return;
        }

        log.info("Checking and seeding initial demo data...");

        // 1. Seed standard system parameters (creator == null)
        Map<String, TestParameter> systemParams = seedSystemParameters();

        // 2. Seed demo user and user-specific data
        seedDemoUserAndResults(systemParams);

        log.info("Demo data seeding completed successfully.");
    }

    private Map<String, TestParameter> seedSystemParameters() {
        Map<String, TestParameter> existingOrCreated = new HashMap<>();

        List<TestParameter> systemDefs = List.of(
                createParamDef("Glukoza", "mg/dl", 70.0, 99.0),
                createParamDef("Morfologia - Hemoglobina", "g/dl", 12.0, 17.5),
                createParamDef("Cholesterol całkowity", "mg/dl", 115.0, 190.0),
                createParamDef("Ciśnienie skurczowe", "mmHg", 90.0, 120.0),
                createParamDef("Ciśnienie rozkurczowe", "mmHg", 60.0, 80.0),
                createParamDef("Witamina D3", "ng/ml", 30.0, 50.0),
                createParamDef("TSH", "µIU/ml", 0.27, 4.2),
                createParamDef("Kwas moczowy", "mg/dl", 3.5, 7.2)
        );

        List<TestParameter> allParams = parameterRepository.findAll();
        for (TestParameter def : systemDefs) {
            TestParameter existing = allParams.stream()
                    .filter(p -> p.getCreator() == null && p.getName().equalsIgnoreCase(def.getName()))
                    .findFirst()
                    .orElse(null);

            if (existing == null) {
                existing = parameterRepository.save(def);
                log.info("Seeded system parameter: {}", existing.getName());
            }
            existingOrCreated.put(existing.getName(), existing);
        }

        return existingOrCreated;
    }

    private TestParameter createParamDef(String name, String unit, Double minNorm, Double maxNorm) {
        TestParameter param = new TestParameter();
        param.setName(name);
        param.setUnit(unit);
        param.setMinNorm(minNorm);
        param.setMaxNorm(maxNorm);
        param.setCreator(null);
        return param;
    }

    private void seedDemoUserAndResults(Map<String, TestParameter> systemParams) {
        final String demoUsername = "demo";

        if (userRepository.findByUsername(demoUsername).isPresent()) {
            log.info("Demo user '{}' already exists. Skipping user seed.", demoUsername);
            return;
        }

        AppUser demoUser = new AppUser();
        demoUser.setUsername(demoUsername);
        demoUser.setPassword(passwordEncoder.encode("demo123"));
        demoUser.setEmail("demo@example.com");
        demoUser = userRepository.save(demoUser);
        log.info("Seeded demo user: username='{}', password='{}'", demoUsername, "demo123");

        // Seed a custom private parameter for demo user
        TestParameter ironParam = new TestParameter();
        ironParam.setName("Żelazo");
        ironParam.setUnit("µg/dl");
        ironParam.setMinNorm(60.0);
        ironParam.setMaxNorm(170.0);
        ironParam.setCreator(demoUser);
        ironParam = parameterRepository.save(ironParam);
        log.info("Seeded custom private parameter for {}: {}", demoUsername, ironParam.getName());

        // Seed realistic historical test results
        List<TestResult> results = new ArrayList<>();

        TestParameter glucose = systemParams.get("Glukoza");
        if (glucose != null) {
            results.add(createResult(88.0, LocalDate.now().minusMonths(4), demoUser, glucose));
            results.add(createResult(94.0, LocalDate.now().minusMonths(3), demoUser, glucose));
            results.add(createResult(106.0, LocalDate.now().minusMonths(2), demoUser, glucose)); // elevated
            results.add(createResult(97.0, LocalDate.now().minusMonths(1), demoUser, glucose));
            results.add(createResult(91.0, LocalDate.now().minusWeeks(1), demoUser, glucose));
        }

        TestParameter hemoglobin = systemParams.get("Morfologia - Hemoglobina");
        if (hemoglobin != null) {
            results.add(createResult(13.8, LocalDate.now().minusMonths(4), demoUser, hemoglobin));
            results.add(createResult(14.1, LocalDate.now().minusMonths(3), demoUser, hemoglobin));
            results.add(createResult(14.4, LocalDate.now().minusMonths(1), demoUser, hemoglobin));
            results.add(createResult(14.0, LocalDate.now().minusWeeks(1), demoUser, hemoglobin));
        }

        TestParameter cholesterol = systemParams.get("Cholesterol całkowity");
        if (cholesterol != null) {
            results.add(createResult(215.0, LocalDate.now().minusMonths(4), demoUser, cholesterol)); // elevated
            results.add(createResult(198.0, LocalDate.now().minusMonths(3), demoUser, cholesterol)); // slightly elevated
            results.add(createResult(184.0, LocalDate.now().minusMonths(1), demoUser, cholesterol));
            results.add(createResult(172.0, LocalDate.now().minusWeeks(1), demoUser, cholesterol));
        }

        TestParameter sysPressure = systemParams.get("Ciśnienie skurczowe");
        if (sysPressure != null) {
            results.add(createResult(132.0, LocalDate.now().minusMonths(3), demoUser, sysPressure)); // elevated
            results.add(createResult(125.0, LocalDate.now().minusMonths(2), demoUser, sysPressure));
            results.add(createResult(119.0, LocalDate.now().minusMonths(1), demoUser, sysPressure));
            results.add(createResult(118.0, LocalDate.now().minusWeeks(1), demoUser, sysPressure));
        }

        // Iron results (custom parameter)
        results.add(createResult(52.0, LocalDate.now().minusMonths(3), demoUser, ironParam)); // low
        results.add(createResult(76.0, LocalDate.now().minusMonths(2), demoUser, ironParam));
        results.add(createResult(110.0, LocalDate.now().minusWeeks(2), demoUser, ironParam));

        resultRepository.saveAll(results);
        log.info("Seeded {} historical test results for user '{}'", results.size(), demoUsername);
    }

    private TestResult createResult(Double value, LocalDate date, AppUser user, TestParameter param) {
        TestResult result = new TestResult();
        result.setValue(value);
        result.setTestDate(date);
        result.setUser(user);
        result.setParameter(param);
        return result;
    }
}

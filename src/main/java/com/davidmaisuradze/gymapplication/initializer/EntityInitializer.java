package com.davidmaisuradze.gymapplication.initializer;

import com.davidmaisuradze.gymapplication.entity.Trainee;
import com.davidmaisuradze.gymapplication.entity.Trainer;
import com.davidmaisuradze.gymapplication.entity.Training;
import com.davidmaisuradze.gymapplication.entity.TrainingType;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class EntityInitializer {
    @Getter
    private Map<Long, Trainee> traineeMap;
    @Getter
    private Map<Long, Trainer> trainerMap;
    @Getter
    private Map<String, Training> trainingMap;

    @Value("${data.file.path.trainee}")
    private String traineeDataPath;
    @Value("${data.file.path.trainer}")
    private String trainerDataPath;
    @Value("${data.file.path.training}")
    private String trainingDataPath;

    @Autowired
    public void setTrainingMap(
            @Qualifier("traineeStorage") Map<Long, Trainee> traineeMap,
            @Qualifier("trainerStorage") Map<Long, Trainer> trainerMap,
            @Qualifier("trainingStorage") Map<String, Training> trainingMap
    ) {
        this.traineeMap = traineeMap;
        this.trainerMap = trainerMap;
        this.trainingMap = trainingMap;
        log.info("Entity maps injected");
    }

    @PostConstruct
    public void initialize() {
        initializeTrainees();
        initializeTrainers();
        initializeTrainings();
        log.info("All data initialized");
    }

    public void initializeTrainees() {
        log.info("Initializing trainees from file: {}", traineeDataPath);
        ClassPathResource resource = new ClassPathResource(traineeDataPath);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("Processing trainee line: {}", line);
                String[] attributes = line.split(" ");
                String firstName = attributes[0];
                String lastName = attributes[1];
                String userName = usernameGenerator(firstName, lastName);
                String password = RandomPassGenerator.generatePassword();
                boolean isActive = true;
                LocalDate dateOfBirth = LocalDate.parse(attributes[2]);
                String address = attributes[3];
                long userId = Long.parseLong(attributes[4]);
                Trainee trainee = Trainee
                        .builder()
                        .firstName(firstName)
                        .lastName(lastName)
                        .username(userName)
                        .password(password)
                        .isActive(isActive)
                        .dateOfBirth(dateOfBirth)
                        .address(address)
                        .userId(userId)
                        .build();

                traineeMap.put(userId, trainee);
                log.info("Trainee added: {} {}", firstName, lastName);
            }
        } catch (IOException e) {
            log.error("Error reading trainee data from file: {}", traineeDataPath, e);
        }
    }

    public void initializeTrainings() {
        log.info("Initializing trainings from file: {}", trainingDataPath);
        ClassPathResource resource = new ClassPathResource(trainingDataPath);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("Processing training line: {}", line);
                String[] attributes = line.split(" ");
                long traineeId = Long.parseLong(attributes[0]);
                long trainerId = Long.parseLong(attributes[1]);
                String trainingName = attributes[2];
                TrainingType trainingType = new TrainingType(attributes[3]);
                LocalDate trainingDate = LocalDate.parse(attributes[4]);
                double duration = Double.parseDouble(attributes[5]);

                Training training = new Training(
                        traineeId,
                        trainerId,
                        trainingName,
                        trainingType,
                        trainingDate,
                        duration
                );
                trainingMap.put(trainingName, training);
                log.info("Training added: {}", trainingName);
            }
        } catch (IOException e) {
            log.error("Error reading training data from file: {}", trainingDataPath, e);
        }
    }

    public void initializeTrainers() {
        log.info("Initializing trainers from file: {}", trainerDataPath);
        ClassPathResource resource = new ClassPathResource(trainerDataPath);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("Processing trainer line: {}", line);
                String[] attributes = line.split(" ");
                String firstName = attributes[0];
                String lastName = attributes[1];
                String userName = usernameGenerator(firstName, lastName);
                String password = RandomPassGenerator.generatePassword();
                boolean isActive = true;
                String specialization = attributes[2];
                long userId = Long.parseLong(attributes[3]);
                Trainer trainer = Trainer
                        .builder()
                        .firstName(firstName)
                        .lastName(lastName)
                        .username(userName)
                        .password(password)
                        .isActive(isActive)
                        .specialization(specialization)
                        .userId(userId)
                        .build();
                trainerMap.put(userId, trainer);
                log.info("Trainer added: {} {}", firstName, lastName);
            }
        } catch (IOException e) {
            log.error("Error reading trainer data from file: {}", trainerDataPath, e);
        }
    }
    private String usernameGenerator(String first, String last) {
        int counter = 0;
        StringBuilder builder = new StringBuilder();
        builder.append(first).append(".").append(last);
        List<String> usernames = new ArrayList<>();
        for (Trainee t : traineeMap.values()) {
            usernames.add(t.getUsername());
        }
        for (Trainer t : trainerMap.values()) {
            usernames.add(t.getUsername());
        }
        while (true) {
            int counterBefore = counter;
            for (String username : usernames) {
                if (username.contentEquals(builder)) {
                    counter++;
                }
            }
            if (counter != 0) {
                builder.setLength(0);
                builder.append(first);
                builder.append(".");
                builder.append(last);
                builder.append(counter);
            }
            if (counterBefore == counter) {
                break;
            }
        }
        return builder.toString();
    }
}

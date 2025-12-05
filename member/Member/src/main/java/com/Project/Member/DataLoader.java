//package com.Project.Member;
//
//import com.Project.Member.Entity.Member;
//import com.Project.Member.Repository.MemberRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
//@Component
//public class DataLoader implements CommandLineRunner {
//
//    @Autowired
//    private MemberRepository memberRepository;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    private static final String[] FIRST_NAMES = {
//        "John", "Jane", "Michael", "Sarah", "David", "Emily", "James", "Jessica",
//        "Robert", "Ashley", "William", "Amanda", "Richard", "Melissa", "Joseph", "Deborah",
//        "Thomas", "Michelle", "Charles", "Carol", "Christopher", "Lisa", "Daniel", "Nancy",
//        "Matthew", "Karen", "Anthony", "Betty", "Mark", "Helen", "Donald", "Sandra",
//        "Steven", "Donna", "Paul", "Carolyn", "Andrew", "Ruth", "Joshua", "Sharon"
//    };
//
//    private static final String[] LAST_NAMES = {
//        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
//        "Rodriguez", "Martinez", "Hernandez", "Lopez", "Wilson", "Anderson", "Thomas", "Taylor",
//        "Moore", "Jackson", "Martin", "Lee", "Thompson", "White", "Harris", "Sanchez",
//        "Clark", "Ramirez", "Lewis", "Robinson", "Walker", "Young", "Allen", "King",
//        "Wright", "Scott", "Torres", "Nguyen", "Hill", "Flores", "Green", "Adams"
//    };
//
//    @Override
//    public void run(String... args) throws Exception {
//        long existingCount = memberRepository.count();
//        if (existingCount >= 5000) {
//            System.out.println("Database already has " + existingCount + " records. Skipping data insertion.");
//            return;
//        }
//
//        int recordsToInsert = 5000 - (int)existingCount;
//        System.out.println("Starting to insert " + recordsToInsert + " member records...");
//        Random random = new Random();
//        List<Member> members = new ArrayList<>();
//        String hashedPassword = passwordEncoder.encode("Password123!");
//
//        for (int i = 0; i < recordsToInsert; i++) {
//            Member member = new Member();
//
//            String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
//            String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
//            String email = "user" + (existingCount + i) + "@example.com";
//            String userName = "user" + (existingCount + i);
//            String phone = generatePhoneNumber(random);
//
//            member.setFirstName(firstName);
//            member.setLastName(lastName);
//            member.setEmail(email);
//            member.setUserName(userName);
//            member.setPassword(hashedPassword);
//            member.setPhone(phone);
//            member.setStatus(Member.memberStatus.ACTIVE);
//
//            members.add(member);
//
//            // Batch insert every 100 records for better performance
//            if (members.size() >= 100) {
//                memberRepository.saveAll(members);
//                System.out.println("Inserted " + (i + 1) + " records...");
//                members.clear();
//            }
//        }
//
//        // Insert remaining records
//        if (!members.isEmpty()) {
//            memberRepository.saveAll(members);
//        }
//
//        long finalCount = memberRepository.count();
//        System.out.println("Successfully inserted " + recordsToInsert + " member records!");
//        System.out.println("Total records in database: " + finalCount);
//    }
//
//    private String generatePhoneNumber(Random random) {
//        return String.format("%d%07d", random.nextInt(9) + 1, random.nextInt(10000000));
//    }
//}
//

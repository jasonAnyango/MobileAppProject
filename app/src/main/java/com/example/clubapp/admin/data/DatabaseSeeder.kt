package com.example.clubapp.admin.data

import com.example.clubapp.model.Club
import com.google.firebase.firestore.FirebaseFirestore

object DatabaseSeeder {

    fun seedClubs() {
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch() // Efficiently writes multiple documents at once

        val sampleClubs = listOf(
            // --- TECH & ACADEMIC ---
            Club(
                name = "Google Developer Student Clubs (GDSC)",
                description = "Peer-to-peer learning environment for Google technologies. We build solutions for local businesses and communities.",
                mission = "To bridge the gap between theory and practice for student developers.",
                leaderId = "placeholder_uid_1",
                isActive = true
            ),
            Club(
                name = "SU-Sec (Cybersecurity Club)",
                description = "A community for ethical hackers and security enthusiasts. We learn penetration testing, network defense, and cryptography.",
                mission = "To create a security-conscious culture and train the next generation of cyber defenders.",
                leaderId = "placeholder_uid_2",
                isActive = true
            ),
            Club(
                name = "Strathmore Debate Society",
                description = "The premier platform for public speaking and argumentation. We participate in national and international debate tournaments.",
                mission = "To foster critical thinking and eloquence among students.",
                leaderId = "placeholder_uid_3",
                isActive = true
            ),
            Club(
                name = "IEEE Student Branch",
                description = "The world's largest technical professional organization dedicated to advancing technology for the benefit of humanity.",
                mission = "To foster technological innovation and excellence.",
                leaderId = "placeholder_uid_4",
                isActive = true
            ),
            Club(
                name = "Strathmore Finance Club",
                description = "Connecting students with the financial world through networking, trading simulations, and guest speaker sessions.",
                mission = "To bridge the gap between classroom finance and the real corporate world.",
                leaderId = "placeholder_uid_5",
                isActive = true
            ),
            Club(
                name = "Robotics & AI Club",
                description = "Designing, building, and programming robots. Exploring Artificial Intelligence applications in hardware.",
                mission = "To innovate through hardware and software integration.",
                leaderId = "placeholder_uid_6",
                isActive = true
            ),
            Club(
                name = "Strathmore Law Clinic",
                description = "Providing legal aid to the community and practical experience for law students.",
                mission = "To use the law as a tool for social justice.",
                leaderId = "placeholder_uid_7",
                isActive = true
            ),

            // --- SPORTS ---
            Club(
                name = "Strathmore Leos (Rugby)",
                description = "The university rugby team. Competing in the Kenya Cup. Training requires high discipline and fitness.",
                mission = "To dominate the league and build character through rugby.",
                leaderId = "placeholder_uid_8",
                isActive = true
            ),
            Club(
                name = "Blades (Basketball)",
                description = "Men and Women basketball teams competing at the top national level.",
                mission = "Excellence on the court and in the classroom.",
                leaderId = "placeholder_uid_9",
                isActive = true
            ),
            Club(
                name = "SU Scorpions (Hockey)",
                description = "The fierce ladies' hockey team of Strathmore University.",
                mission = "To be the leading women's hockey club in the region.",
                leaderId = "placeholder_uid_10",
                isActive = true
            ),
            Club(
                name = "Karate Club",
                description = "Learn self-defense, discipline, and physical fitness through the art of Karate.",
                mission = "To cultivate strength of body and mind.",
                leaderId = "placeholder_uid_11",
                isActive = true
            ),
            Club(
                name = "Chess Club",
                description = "For strategists and thinkers. We host weekly blitz tournaments and inter-university friendlies.",
                mission = "To improve cognitive abilities through the royal game.",
                leaderId = "placeholder_uid_12",
                isActive = true
            ),

            // --- ARTS & CULTURE ---
            Club(
                name = "Strathmore Chorale",
                description = "A choir dedicated to choral music excellence, performing classical and African pieces.",
                mission = "To touch hearts through harmonious music.",
                leaderId = "placeholder_uid_13",
                isActive = true
            ),
            Club(
                name = "DramSoc (Drama Society)",
                description = "The stage is yours. Acting, scriptwriting, directing, and stage management.",
                mission = "To tell African stories through compelling theater.",
                leaderId = "placeholder_uid_14",
                isActive = true
            ),
            Club(
                name = "SU Band",
                description = "A collective of instrumentalists and vocalists providing entertainment for university events.",
                mission = "To entertain and inspire through live music.",
                leaderId = "placeholder_uid_15",
                isActive = true
            ),

            // --- SOCIAL & SERVICE ---
            Club(
                name = "AIESEC Strathmore",
                description = "Global youth network developing leadership capabilities through cross-cultural exchanges.",
                mission = "To achieve peace and fulfillment of humankind's potential.",
                leaderId = "placeholder_uid_16",
                isActive = true
            ),
            Club(
                name = "President's Award",
                description = "A self-development program focusing on skills, service, physical recreation, and adventurous journeys.",
                mission = "To equip young people for life.",
                leaderId = "placeholder_uid_17",
                isActive = true
            ),
            Club(
                name = "Red Cross Chapter",
                description = "First aid training, disaster response, and blood donation drives.",
                mission = "To alleviate human suffering and save lives.",
                leaderId = "placeholder_uid_18",
                isActive = true
            ),
            Club(
                name = "Environmental Club",
                description = "Promoting sustainability, tree planting, and clean energy initiatives on campus.",
                mission = "To protect and conserve our environment for future generations.",
                leaderId = "placeholder_uid_19",
                isActive = true
            ),

            // --- INACTIVE SAMPLE ---
            Club(
                name = "Old Film Photography Club",
                description = "Legacy club for analog photography enthusiasts. Currently dormant.",
                mission = "To preserve the art of film.",
                leaderId = "placeholder_uid_20",
                isActive = false // Sample inactive club
            )
        )

        // Upload Loop
        sampleClubs.forEach { club ->
            val ref = db.collection("clubs").document() // Generate random ID
            club.id = ref.id
            batch.set(ref, club)
        }

        batch.commit().addOnSuccessListener {
            println("✅ SUCCESS: 20 Sample Clubs Uploaded to Firestore!")
        }.addOnFailureListener {
            println("❌ ERROR: Failed to upload clubs: ${it.message}")
        }
    }
}
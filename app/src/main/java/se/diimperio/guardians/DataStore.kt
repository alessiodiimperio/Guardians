package se.diimperio.guardians

import com.google.firebase.firestore.FirebaseFirestore
import models.Guardian

object DataStore {

    //Firestore db ref
    val db = FirebaseFirestore.getInstance()
    val guardians = mutableListOf<Guardian>()

    init {
        createMockData()
    }

    fun createMockData(){
        val mother = Guardian("Jane", "Smith", "0707106660", "mother@lastname.se", "Parent")
        val father = Guardian("Jack", "Smith", "0707106660", "father@lastname.se", "Parent")
        val sister = Guardian("Jim", "Smith", "0707106660", "sister@lastname.se", "Sister")
        val brother = Guardian("Janet", "Smith", "0707106660", "brother@lastname.se", "Brother")
        guardians.add(mother)
        guardians.add(father)
        guardians.add(brother)
        guardians.add(sister)
    }
}
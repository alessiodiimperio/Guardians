package models

class User(var uid:String? = null, var name:String? = null, var email:String? = null, var number:String? = null, var location:MyLatLng? = null, var token:String? = null, var guardians:MutableList<Guardian> = mutableListOf())

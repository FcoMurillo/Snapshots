package com.example.snapshots

import com.google.firebase.database.IgnoreExtraProperties

//@IgnoreExtraProperties lo recomienda firebase
@IgnoreExtraProperties
data class Snapshot(var id: String = "",
                    var title: String = "",
                    var photoUrl: String = "",
                    var likeList: Map<String, Boolean> = mutableMapOf())

package S.N.R.I.tracking.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WebUrl (
    @PrimaryKey(autoGenerate = true) val uid: Long,
    @ColumnInfo(name = "title") val name: String?,
    @ColumnInfo(name = "url") val url: String?
    )

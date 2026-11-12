package com.studentcompanion.app.data.mapper

import com.google.firebase.Timestamp
import com.studentcompanion.app.data.model.TaskDto
import com.studentcompanion.app.data.model.UserDto
import com.studentcompanion.app.domain.model.TaskStatus
import com.studentcompanion.app.domain.model.TaskType
import com.studentcompanion.app.domain.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Test

class MappersTest {

    @Test
    fun `UserDto with a valid role string maps to the matching enum`() {
        val dto = UserDto(id = "u1", username = "ahmad", fullName = "Ahmad", role = "PARENT")
        assertEquals(UserRole.PARENT, dto.toDomain().role)
    }

    @Test
    fun `UserDto with an unknown or missing role string defaults to STUDENT`() {
        val dto = UserDto(id = "u1", username = "ahmad", fullName = "Ahmad", role = "")
        assertEquals(UserRole.STUDENT, dto.toDomain().role)
    }

    @Test
    fun `TaskDto maps type and status strings to their enums`() {
        val dto = TaskDto(
            id = "t1",
            studentId = "s1",
            type = "PRAYER",
            status = "APPROVED",
            dueAt = Timestamp(100, 0),
        )
        val task = dto.toDomain()
        assertEquals(TaskType.PRAYER, task.type)
        assertEquals(TaskStatus.APPROVED, task.status)
    }

    @Test
    fun `TaskDto with an unrecognized status falls back to PENDING rather than throwing`() {
        val dto = TaskDto(
            id = "t1",
            studentId = "s1",
            type = "HOMEWORK",
            status = "SOME_FUTURE_STATUS",
            dueAt = Timestamp(100, 0),
        )
        assertEquals(TaskStatus.PENDING, dto.toDomain().status)
    }

    @Test
    fun `Firestore Timestamp converts to the equivalent Instant`() {
        val dto = TaskDto(
            id = "t1",
            studentId = "s1",
            type = "PRAYER",
            status = "PENDING",
            dueAt = Timestamp(1_700_000_000, 500_000_000),
        )
        val instant = dto.toDomain().dueAt
        assertEquals(1_700_000_000L, instant.epochSecond)
        assertEquals(500_000_000, instant.nano)
    }
}

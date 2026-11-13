# Gson fills the REST DTOs by reflection on field names, so R8 must not rename
# or strip them — otherwise every backend response parses to empty objects in
# release builds only.
-keep class com.studentcompanion.app.data.remote.rest.dto.** { *; }

package io.github.ygaray.yahirandroidtaste.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumSourcePickerSheet(
    onNavigateToCamera: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    SheetScaffold(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text("Gallery") },
                leadingContent = {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToGallery(); onDismiss() }
            )
            ListItem(
                headlineContent = { Text("Camera") },
                leadingContent = {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToCamera(); onDismiss() }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

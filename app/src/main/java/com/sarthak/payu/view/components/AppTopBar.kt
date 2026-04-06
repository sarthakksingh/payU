package com.sarthak.payu.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PayUTopBar(
    notificationCount: Int = 0,
    onSearchClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text("P", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 18.sp)
        }
        Spacer(Modifier.width(10.dp))
        Text("PayU", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onSearchClick) {
            Icon(
                androidx.compose.material.icons.Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.White
            )
        }
        Box {
            IconButton(onClick = onNotificationClick) {
                Icon(
                    androidx.compose.material.icons.Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.White
                )
            }
            if (notificationCount > 0) {
                Badge(
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    containerColor = Color(0xFFEF4444)
                ) {
                    Text("$notificationCount", fontSize = 10.sp)
                }
            }
        }
    }
}
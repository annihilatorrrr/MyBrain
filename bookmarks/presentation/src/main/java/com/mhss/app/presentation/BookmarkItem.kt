package com.mhss.app.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mhss.app.domain.model.Bookmark
import com.mhss.app.ui.Res
import com.mhss.app.ui.ic_open_link
import com.mhss.app.ui.open_link
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun BookmarkItem(
    modifier: Modifier = Modifier,
    bookmark: Bookmark,
    onClick: (Bookmark) -> Unit,
    onInvalidUrl: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(
            8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .clickable { onClick(bookmark) }
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            if (bookmark.title.isNotBlank()){
                Text(
                    bookmark.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
            }
            Text(
                bookmark.url,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.Gray
            )
            IconButton(
                onClick = {
                    if (bookmark.url.isValidUrl()){
                        uriHandler.openUri(
                            if (!bookmark.url.startsWith("https://") && !bookmark.url.startsWith("http://")) "http://${bookmark.url}" else bookmark.url
                        )
                    } else
                        onInvalidUrl()
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    painterResource(Res.drawable.ic_open_link),
                    stringResource(Res.string.open_link)
                )
            }
        }
    }
}
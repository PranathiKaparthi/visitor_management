package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun QrCodeDraw(
    content: String,
    modifier: Modifier = Modifier,
    qrColor: Color = MaterialTheme.colorScheme.onSurface,
    bgColor: Color = Color.Transparent
) {
    Canvas(
        modifier = modifier
            .size(160.dp)
            .padding(12.dp)
    ) {
        val sizePx = size.width
        val gridCount = 17 // 17x17 grid
        val cellSize = sizePx / gridCount

        // 1. Draw Background if needed
        if (bgColor != Color.Transparent) {
            drawRect(
                color = bgColor,
                size = Size(sizePx, sizePx)
            )
        }

        // 2. Generate matrix based on content string hash
        val contentHash = content.hashCode()
        fun getBit(row: Int, col: Int): Boolean {
            // Finder pattern at top-left 7x7
            if (row < 7 && col < 7) {
                return (row == 0 || row == 6 || col == 0 || col == 6) || (row >= 2 && row <= 4 && col >= 2 && col <= 4)
            }
            // Finder pattern at top-right 7x7
            if (row < 7 && col >= gridCount - 7) {
                val cCol = col - (gridCount - 7)
                return (row == 0 || row == 6 || cCol == 0 || cCol == 6) || (row >= 2 && row <= 4 && cCol >= 2 && cCol <= 4)
            }
            // Finder pattern at bottom-left 7x7
            if (row >= gridCount - 7 && col < 7) {
                val rRow = row - (gridCount - 7)
                return (rRow == 0 || rRow == 6 || col == 0 || col == 6) || (rRow >= 2 && rRow <= 4 && col >= 2 && col <= 4)
            }

            // Pseudo-random data blocks based on hashing
            val index = row * gridCount + col
            val mixedHash = contentHash xor (index * 0x7E3779B9)
            return (mixedHash % 2 == 0)
        }

        // 3. Draw the cells
        for (r in 0 until gridCount) {
            for (c in 0 until gridCount) {
                if (getBit(r, c)) {
                    drawRect(
                        color = qrColor,
                        topLeft = Offset(c * cellSize, r * cellSize),
                        size = Size(cellSize + 0.5f, cellSize + 0.5f) // overlapping avoids fine white lines
                    )
                }
            }
        }
    }
}

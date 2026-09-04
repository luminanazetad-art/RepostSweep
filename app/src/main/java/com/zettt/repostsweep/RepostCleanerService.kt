package com.zettt.repostsweep

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class RepostCleanerService : AccessibilityService() {

    companion object {
        var isRunning = false
        var deletedCount = 0
    }

    private val handler = Handler(Looper.getMainLooper())
    private var busy = false

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!isRunning || busy) return
        val root = rootInActiveWindow ?: return
        busy = true
        handler.postDelayed({
            processScreen(root)
            busy = false
        }, 500) // kasih jeda biar UI TikTok selesai render dulu
    }

    private fun processScreen(root: AccessibilityNodeInfo) {
        // Tahap 3: dialog konfirmasi "Hapus postingan ulang"
        val confirmBtn = findNodeByText(root, "Hapus postingan ulang")
        if (confirmBtn != null) {
            clickNode(confirmBtn)
            deletedCount++
            handler.postDelayed({ performGlobalAction(GLOBAL_ACTION_BACK) }, 1000)
            return
        }

        // Tahap 2: bottom sheet "Postingan ulang" -> tombol "Hapus"
        val hapusBtn = findNodeByText(root, "Hapus")
        val postinganUlangLabel = findNodeByText(root, "Postingan ulang")
        if (hapusBtn != null && postinganUlangLabel != null) {
            clickNode(hapusBtn)
            return
        }

        // Tahap 1: label "Anda memposting ulang" di video detail
        val repostLabel = findNodeByText(root, "Anda memposting ulang")
        if (repostLabel != null) {
            clickNode(repostLabel)
            return
        }

        // Tahap 0: masih di grid repost -> tap video pertama
        val firstVideo = findFirstClickableGridItem(root)
        if (firstVideo != null) {
            clickNode(firstVideo)
        }
    }

    private fun findNodeByText(root: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        val list = root.findAccessibilityNodeInfosByText(text)
        return list.firstOrNull()
    }

    private fun findFirstClickableGridItem(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        // Cari node pertama yang clickable dan cukup besar (thumbnail video), skip tombol navigasi kecil
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            val found = searchClickable(child)
            if (found != null) return found
        }
        return null
    }

    private fun searchClickable(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isClickable && node.className?.contains("FrameLayout") == true) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = searchClickable(child)
            if (result != null) return result
        }
        return null
    }

    private fun clickNode(node: AccessibilityNodeInfo) {
        var target: AccessibilityNodeInfo? = node
        while (target != null && !target.isClickable) {
            target = target.parent
        }
        target?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    override fun onInterrupt() {}
}

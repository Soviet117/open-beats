package com.soviet117.openbeats

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSDate
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun currentHour(): Int {
    val calendar = NSCalendar.currentCalendar
    return calendar.component(NSCalendarUnitHour, fromDate = NSDate()).toInt()
}
//
//  DictionaryExtention.swift
//  Workruta
//
//  Created by The KING on 09/06/2022.
//

import UIKit
import GooglePlaces
import Foundation
import SwiftUI

extension View {
    
    func cornerRadius(_ radius: CGFloat, corners: UIRectCorner) -> some View {
        clipShape(RoundedCorners(radius: radius, corners: corners))
    }
    
    func customPickerStyle(index: Binding<Int>, items: [String], font: SwiftUI.Font, padding: CGFloat) -> some View {
        self.modifier(CustomPickerStyle(index: index, items: items, font: font, padding: padding))
    }
    
    func backgroundImage(imageName: String, rotateTo: Double = 0, opacity: Double = 1) -> some View {
        return background(
            Image(imageName)
                .resizable()
                .aspectRatio(contentMode: .fill)
                .opacity(opacity)
                .rotationEffect(.degrees(rotateTo))
        )
    }
    
    func backgroundImage(imageName: String, cornerRadius: CGFloat = 0) -> some View {
        return background(
            Image(imageName).resizable().scaledToFill().clipShape(RoundedRectangle(cornerRadius: cornerRadius))
        )
    }
    
    func backgroundImage(systemName: String, width: CGFloat = 20, height: CGFloat = 20) -> some View {
        return background(
            Image(systemName: systemName).resizable().scaledToFill().frame(width: width, height: height)
        )
    }
    
    func border(width: CGFloat, edges: [Edge], color: Color) -> some View {
        overlay(EdgeBorder(width: width, edges: edges).foregroundColor(color))
    }
    
    func padding(axis: Axis, value: CGFloat = 0) -> some View {
        var index = 0
        var dict = [self]
        if axis == .vertical{
            index = 1
            let vStack = VStack(spacing: 0) {
                Spacer().frame(height: value)
                self
                Spacer().frame(height: value)
            }
            dict[1] = vStack as! Self
        } else if axis == .horizontal{
            index = 1
            let hStack = HStack(spacing: 0) {
                Spacer().frame(width: value)
                self
                Spacer().frame(width: value)
            }
            dict[1] = hStack as! Self
        }
        return dict[index]
    }
    
    func padding(sides: [Sides], value: CGFloat = 0) -> some View {
        let hStack = HStack(spacing: 0) {
            if sides.contains(.left) {
                Spacer().frame(width: value)
            }
            self
            if sides.contains(.right) {
                Spacer().frame(width: value)
            }
        }
        return VStack(spacing: 0) {
            if sides.contains(.top) {
                Spacer().frame(height: value)
            }
            hStack
            if sides.contains(.bottom) {
                Spacer().frame(height: value)
            }
        }
    }
    
    func padding(left: CGFloat = 0, top: CGFloat = 0, right: CGFloat = 0, bottom: CGFloat = 0) -> some View {
        let hStack = HStack(spacing: 0) {
            Spacer().frame(width: left)
            self
            Spacer().frame(width: right)
        }
        return VStack(spacing: 0) {
            Spacer().frame(height: top)
            hStack
            Spacer().frame(height: bottom)
        }
    }
    
    func padding(vertical: CGFloat = 0, horizontal: CGFloat = 0) -> some View {
        let hStack = HStack(spacing: 0) {
            Spacer().frame(width: horizontal)
            self
            Spacer().frame(width: horizontal)
        }
        return VStack(spacing: 0) {
            Spacer().frame(height: vertical)
            hStack
            Spacer().frame(height: vertical)
        }
    }
}

extension UIViewController {
    
    func finish() {
        self.dismiss(animated: true)
    }
    
    func time() -> Int64 {
        return Int64(Date().timeIntervalSince1970)
    }
    
    func showAlertBox(title: String, msg: String, btnText: String){
        let alertBox = UIAlertController(title: title, message: msg, preferredStyle: .alert)
        alertBox.addAction(UIAlertAction(title: btnText, style: .default))
        self.present(alertBox, animated: true, completion: nil)
    }
    
    func makeToast(view: UIView, msg: String){
        let x = (view.frame.size.width / 2) - 75
        let y = view.frame.size.height - 100
        let width = view.frame.size.width - 40
        let height = 35.0
        let toastLabel = UILabel(frame: CGRect(x: x, y: y, width: width, height: height))
        toastLabel.backgroundColor = UIColor.black.withAlphaComponent(0.6)
        toastLabel.textColor = UIColor.white
        toastLabel.font = .systemFont(ofSize: 12.0)
        toastLabel.textAlignment = .center
        toastLabel.text = msg
        toastLabel.alpha = 1.0
        toastLabel.layer.cornerRadius = 10
        toastLabel.clipsToBounds = true
        view.addSubview(toastLabel)
        UIView.animate(withDuration: 4.0, delay: 0.1, options: .curveEaseOut) {
            toastLabel.alpha = 0.0
        } completion: { (isCompleted) in
            toastLabel.removeFromSuperview()
        }
    }
}

extension String {
    
    static let shortDate: DateFormatter = {
        let formatter = DateFormatter()
        formatter.calendar = Calendar(identifier: .iso8601)
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.dateStyle = .short
        return formatter
    }()
    
    var shortDate: Date? {
        return String.shortDate.date(from: self)
    }
    
    var htmlToAttributedString: NSAttributedString? {
        guard let data = data(using: .utf8) else {
            return nil
        }
        do {
            return try NSAttributedString(data: data, options: [.documentType: NSAttributedString.DocumentType.html, .characterEncoding:String.Encoding.utf8.rawValue], documentAttributes: nil)
        } catch {
            return nil
        }
    }
    var htmlToString: String {
        return htmlToAttributedString?.string ?? ""
    }
    
    func stripeToDollar() -> String {
        let cost = Double(self)
        let dollar = cost! / 100
        return "$\(dollar)"
    }
    
    func safeEmail() -> String {
        var safeEmail = self.replacingOccurrences(of: ".", with: "-")
        safeEmail = safeEmail.replacingOccurrences(of: "@", with: "-")
        return safeEmail
    }
    
    func safeUrl() -> String {
        var safeUrl = self.replacingOccurrences(of: "/", with: "~")
        safeUrl = safeUrl.replacingOccurrences(of: ".", with: "_")
        return safeUrl
    }
    
    func rawUrl() -> String {
        var rawUrl = self.replacingOccurrences(of: "~", with: "/")
        rawUrl = rawUrl.replacingOccurrences(of: "_", with: ".")
        return rawUrl
    }
    
    func subString(bounds: CountableClosedRange<Int>) -> String {
        let start = index(startIndex, offsetBy: bounds.lowerBound)
        let end = index(startIndex, offsetBy: bounds.upperBound)
        let subStr = self[start...end]
        return String(subStr)
    }
    
    func subString(bounds: CountableRange<Int>) -> String {
        let start = index(startIndex, offsetBy: bounds.lowerBound)
        let end = index(startIndex, offsetBy: bounds.upperBound)
        let subStr = self[start..<end]
        return String(subStr)
    }
    
    func convertToDateArray() -> [Int] {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        let date = dateFormatter.date(from: self)
        let calendar = Calendar.current
        let components = calendar.dateComponents([.year, .month, .day, .hour, .minute, .second], from: date!)
        return [
            components.year!,
            components.month!,
            components.day!,
            components.hour!,
            components.minute!,
            components.second!
        ]
    }
    
    func convertToDate() -> Date {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
        let date = dateFormatter.date(from: self)
        return date!
    }
    
}

extension Int {
    func stringDouble() -> String {
        var num = String(self)
        if self < 10 {
            num = "0" + String(self)
        }
        return num
    }
}

extension Date {
    
    func minify() -> String {
        let numOfSecs = Date().timeIntervalSince(self)
        var dateStr = ""
        if numOfSecs < 60 {
            dateStr = "Just now"
        } else {
            let min = Int(numOfSecs / 60)
            if min == 1 {
                dateStr = "1 minute ago"
            } else {
                if min < 60 {
                    dateStr = String(min) + " minutes ago"
                } else {
                    let hr = Int(min / 60)
                    if hr == 1 {
                        dateStr = "1 hour ago"
                    } else {
                        if hr < 24 {
                            dateStr = String(hr) + " hours ago"
                        } else {
                            let isYstrdy = Calendar.current.isDateInYesterday(self)
                            if isYstrdy {
                                dateStr = "Yesterday"
                            } else {
                                let calendar = Calendar.current
                                let components = calendar.dateComponents([.year, .month, .day, .hour, .minute], from: self)
                                let days = Int(hr / 24)
                                if days < 7 {
                                    let weekDay = calendar.dateComponents([.weekday], from: self).weekday! - 1
                                    let dayOfWeek = Constants.days[weekDay]
                                    dateStr = dayOfWeek
                                } else {
                                    let componentsExtra = calendar.dateComponents([.year], from: Date())
                                    let year: Int = components.year!
                                    let month: Int = components.month! - 1
                                    let yearExtra: Int = componentsExtra.year!
                                    let mnth = Constants.months[month]
                                    if yearExtra > year {
                                        dateStr = "\(year) "
                                    }
                                    dateStr += mnth
                                }
                            }
                        }
                    }
                }
            }
        }
        return dateStr
    }
    
    func miniDate() -> String {
        let numOfSecs = Date().timeIntervalSince(self)
        var dateStr = ""
        if numOfSecs < 60 {
            dateStr = "Just now"
        } else {
            let min = Int(numOfSecs / 60)
            if min == 1 {
                dateStr = "1 minute ago"
            } else {
                if min < 60 {
                    dateStr = String(min) + " minutes ago"
                } else {
                    let hr = Int(min / 60)
                    if hr == 1 {
                        dateStr = "1 hour ago"
                    } else {
                        if hr < 24 {
                            dateStr = String(hr) + " hours ago"
                        } else {
                            let calendar = Calendar.current
                            let isYstrdy = Calendar.current.isDateInYesterday(self)
                            let components = calendar.dateComponents([.year, .month, .day, .hour, .minute], from: self)
                            let hour: Int = components.hour!
                            let minute: Int = components.minute!
                            var prompt = hour > 12 ? "pm" : "am"
                            prompt = hour == 12 ? "noon" : prompt
                            let h = hour > 12 ? hour - 12 : hour
                            let hStr = h < 10 ? "0" + String(h) : String(h)
                            let mStr = minute < 10 ? ":0" + String(minute) : ":" + String(minute)
                            let time = " at " + hStr + mStr + prompt
                            if isYstrdy {
                                dateStr = "Yesterday" + time
                            } else {
                                let days = Int(hr / 24)
                                if days < 7 {
                                    let weekDay = calendar.dateComponents([.weekday], from: self).weekday! - 1
                                    let dayOfWeek = Constants.days[weekDay]
                                    dateStr = dayOfWeek + time
                                } else {
                                    let componentsExtra = calendar.dateComponents([.year], from: Date())
                                    let year: Int = components.year!
                                    let month: Int = components.month! - 1
                                    let yearExtra: Int = componentsExtra.year!
                                    let mnth = Constants.months[month]
                                    if yearExtra > year {
                                        dateStr = "\(year) "
                                    }
                                    dateStr += mnth + time
                                }
                            }
                        }
                    }
                }
            }
        }
        return dateStr
    }
    
    func toShortString() -> String{
        let calendar = Calendar.current
        let components = calendar.dateComponents([.year, .month, .day, .hour, .minute], from: self)
        let year: Int = components.year!
        let month: Int = components.month!
        let day: Int = components.day!
        let dateStr = String(day) + " " + Constants.months[month - 1] + " " + String(year)
        return dateStr
    }
    
    func friendlyString() -> String{
        let calendar = Calendar.current
        let components = calendar.dateComponents([.year, .month, .day, .hour, .minute], from: self)
        let year: Int = components.year!
        let month: Int = components.month!
        let day: Int = components.day!
        let hour: Int = components.hour!
        let minute: Int = components.minute!
        var dateStr = String(day) + " " + Constants.months[month - 1] + " " + String(year)
        var hr = String(hour)
        var mn = String(minute)
        if hour < 10 {
            hr = "0" + String(hour)
        }
        if minute < 10 {
            mn = "0" + String(minute)
        }
        dateStr += " (" + hr + ":" + mn + ")"
        return dateStr
    }
    
    func dateTimeStamp() -> String {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:00"
        let dateStr = dateFormatter.string(from: self)
        return dateStr
    }
    
    func dateToLong() -> Int32 {
        return Int32(self.timeIntervalSince1970)
    }
    
}

extension Double {
    func roundUp(to places: Int) -> Double {
        let divisor = pow(10.0, Double(places))
        return (self * divisor).rounded() / divisor
    }
}

extension Dictionary {
    
    var toQueryString: String {
        return self.reduce(""){
            "\($0)\($1.0)=\($1.1)&"
        }
    }
    
    func containsKey(_ key: String) -> Bool {
        return self[key as! Key] != nil
    }
    
    func allKeys() -> [String] {
        //self.forEach{print($0)}
        var keyArray = [String]()
        self.forEach { key, _ in
            let k = key as! String
            keyArray.append(k)
        }
        return keyArray
    }
    
}

extension UIImageView {

    public func loadGif(name: String) {
        DispatchQueue.global().async {
            let image = UIImage.gif(name: name)
            DispatchQueue.main.async {
                self.image = image
            }
        }
    }

    @available(iOS 9.0, *)
    public func loadGif(asset: String) {
        DispatchQueue.global().async {
            let image = UIImage.gif(asset: asset)
            DispatchQueue.main.async {
                self.image = image
            }
        }
    }

}

extension UIImage {
    
    var getWidth: CGFloat {
        get {
            return self.size.width
        }
    }
    
    var getHeight: CGFloat {
        get {
            return self.size.height
        }
    }

    public class func gif(data: Data) -> UIImage? {
        // Create source from data
        guard let source = CGImageSourceCreateWithData(data as CFData, nil) else {
            print("SwiftGif: Source for the image does not exist")
            return nil
        }

        return UIImage.animatedImageWithSource(source)
    }

    public class func gif(url: String) -> UIImage? {
        // Validate URL
        guard let bundleURL = URL(string: url) else {
            print("SwiftGif: This image named \"\(url)\" does not exist")
            return nil
        }

        // Validate data
        guard let imageData = try? Data(contentsOf: bundleURL) else {
            print("SwiftGif: Cannot turn image named \"\(url)\" into NSData")
            return nil
        }

        return gif(data: imageData)
    }

    public class func gif(name: String) -> UIImage? {
        // Check for existance of gif
        guard let bundleURL = Bundle.main
          .url(forResource: name, withExtension: "gif") else {
            print("SwiftGif: This image named \"\(name)\" does not exist")
            return nil
        }

        // Validate data
        guard let imageData = try? Data(contentsOf: bundleURL) else {
            print("SwiftGif: Cannot turn image named \"\(name)\" into NSData")
            return nil
        }

        return gif(data: imageData)
    }

    @available(iOS 9.0, *)
    public class func gif(asset: String) -> UIImage? {
        // Create source from assets catalog
        guard let dataAsset = NSDataAsset(name: asset) else {
            print("SwiftGif: Cannot turn image named \"\(asset)\" into NSDataAsset")
            return nil
        }

        return gif(data: dataAsset.data)
    }

    internal class func delayForImageAtIndex(_ index: Int, source: CGImageSource!) -> Double {
        var delay = 0.1

        // Get dictionaries
        let cfProperties = CGImageSourceCopyPropertiesAtIndex(source, index, nil)
        let gifPropertiesPointer = UnsafeMutablePointer<UnsafeRawPointer?>.allocate(capacity: 0)
        defer {
            gifPropertiesPointer.deallocate()
        }
        let unsafePointer = Unmanaged.passUnretained(kCGImagePropertyGIFDictionary).toOpaque()
        if CFDictionaryGetValueIfPresent(cfProperties, unsafePointer, gifPropertiesPointer) == false {
            return delay
        }

        let gifProperties: CFDictionary = unsafeBitCast(gifPropertiesPointer.pointee, to: CFDictionary.self)

        // Get delay time
        var delayObject: AnyObject = unsafeBitCast(
            CFDictionaryGetValue(gifProperties,
                Unmanaged.passUnretained(kCGImagePropertyGIFUnclampedDelayTime).toOpaque()),
            to: AnyObject.self)
        if delayObject.doubleValue == 0 {
            delayObject = unsafeBitCast(CFDictionaryGetValue(gifProperties,
                Unmanaged.passUnretained(kCGImagePropertyGIFDelayTime).toOpaque()), to: AnyObject.self)
        }

        if let delayObject = delayObject as? Double, delayObject > 0 {
            delay = delayObject
        } else {
            delay = 0.1 // Make sure they're not too fast
        }

        return delay
    }

    internal class func gcdForPair(_ lhs: Int?, _ rhs: Int?) -> Int {
        var lhs = lhs
        var rhs = rhs
        // Check if one of them is nil
        if rhs == nil || lhs == nil {
            if rhs != nil {
                return rhs!
            } else if lhs != nil {
                return lhs!
            } else {
                return 0
            }
        }

        // Swap for modulo
        if lhs! < rhs! {
            let ctp = lhs
            lhs = rhs
            rhs = ctp
        }

        // Get greatest common divisor
        var rest: Int
        while true {
            rest = lhs! % rhs!

            if rest == 0 {
                return rhs! // Found it
            } else {
                lhs = rhs
                rhs = rest
            }
        }
    }

    internal class func gcdForArray(_ array: [Int]) -> Int {
        if array.isEmpty {
            return 1
        }

        var gcd = array[0]

        for val in array {
            gcd = UIImage.gcdForPair(val, gcd)
        }

        return gcd
    }

    internal class func animatedImageWithSource(_ source: CGImageSource) -> UIImage? {
        let count = CGImageSourceGetCount(source)
        var images = [CGImage]()
        var delays = [Int]()

        // Fill arrays
        for index in 0..<count {
            // Add image
            if let image = CGImageSourceCreateImageAtIndex(source, index, nil) {
                images.append(image)
            }

            // At it's delay in cs
            let delaySeconds = UIImage.delayForImageAtIndex(Int(index),
                source: source)
            delays.append(Int(delaySeconds * 1000.0)) // Seconds to ms
        }

        // Calculate full duration
        let duration: Int = {
            var sum = 0

            for val: Int in delays {
                sum += val
            }

            return sum
            }()

        // Get frames
        let gcd = gcdForArray(delays)
        var frames = [UIImage]()

        var frame: UIImage
        var frameCount: Int
        for index in 0..<count {
            frame = UIImage(cgImage: images[Int(index)])
            frameCount = Int(delays[Int(index)] / gcd)

            for _ in 0..<frameCount {
                frames.append(frame)
            }
        }

        // Heyhey
        let animation = UIImage.animatedImage(with: frames,
            duration: Double(duration) / 1000.0)

        return animation
    }
    
    /// Fix image orientaton to protrait up
    func fixedOrientation() -> UIImage? {
        guard imageOrientation != UIImage.Orientation.up else {
            // This is default orientation, don't need to do anything
            return self.copy() as? UIImage
        }

        guard let cgImage = self.cgImage else {
            // CGImage is not available
            return nil
        }

        guard let colorSpace = cgImage.colorSpace, let ctx = CGContext(data: nil, width: Int(size.width), height: Int(size.height), bitsPerComponent: cgImage.bitsPerComponent, bytesPerRow: 0, space: colorSpace, bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue) else {
            return nil // Not able to create CGContext
        }

        var transform: CGAffineTransform = CGAffineTransform.identity

        switch imageOrientation {
        case .down, .downMirrored:
            transform = transform.translatedBy(x: size.width, y: size.height)
            transform = transform.rotated(by: CGFloat.pi)
        case .left, .leftMirrored:
            transform = transform.translatedBy(x: size.width, y: 0)
            transform = transform.rotated(by: CGFloat.pi / 2.0)
        case .right, .rightMirrored:
            transform = transform.translatedBy(x: 0, y: size.height)
            transform = transform.rotated(by: CGFloat.pi / -2.0)
        case .up, .upMirrored:
            break
        @unknown default:
            fatalError("Missing...")
            break
        }

        // Flip image one more time if needed to, this is to prevent flipped image
        switch imageOrientation {
        case .upMirrored, .downMirrored:
            transform = transform.translatedBy(x: size.width, y: 0)
            transform = transform.scaledBy(x: -1, y: 1)
        case .leftMirrored, .rightMirrored:
            transform = transform.translatedBy(x: size.height, y: 0)
            transform = transform.scaledBy(x: -1, y: 1)
        case .up, .down, .left, .right:
            break
        @unknown default:
            fatalError("Missing...")
            break
        }

        ctx.concatenate(transform)

        switch imageOrientation {
        case .left, .leftMirrored, .right, .rightMirrored:
            ctx.draw(cgImage, in: CGRect(x: 0, y: 0, width: size.height, height: size.width))
        default:
            ctx.draw(cgImage, in: CGRect(x: 0, y: 0, width: size.width, height: size.height))
            break
        }

        guard let newCGImage = ctx.makeImage() else { return nil }
        return UIImage.init(cgImage: newCGImage, scale: 1, orientation: .up)
    }

}

extension UIApplication {
    func hideKeyboard(hide: Bool) {
        if hide {
            sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
        }
    }
}

extension FloatingPoint {
    var whole: Self {
        modf(self).0
    }
    var fraction: Self {
        modf(self).1
    }
}


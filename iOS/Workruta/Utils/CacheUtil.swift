//
//  CacheUtil.swift
//  Workruta
//
//  Created by The KING on 16/06/2022.
//

import Foundation

class CacheUtil {
    
    private let fileManager = FileManager.default
    private let cacheDir = "Workruta"
    private let cacheFile = "cache.txt"
    private var cacher = NSCache<NSString, NSData>()
    
    private func createDir(){
        let dirUrl = URL(string: self.documentDirectory())!
        let dirPath = dirUrl.appendingPathComponent(self.cacheDir)
        if !fileDoExists(filePath: dirPath.path){
            do {
                try self.fileManager.createDirectory(atPath: dirPath.path, withIntermediateDirectories: true, attributes: nil)
            } catch {
                print("Unable to create dir")
            }
        }
    }
    
    private func createFile(){
        let dirUrl = URL(string: self.documentDirectory())!
        let dirPath = dirUrl.appendingPathComponent(self.cacheDir + "/" + self.cacheFile)
        if !fileDoExists(filePath: dirPath.path){
            do {
                try self.fileManager.createDirectory(atPath: dirPath.path, withIntermediateDirectories: true, attributes: nil)
            } catch {
                print("Unable to create file")
            }
        }
    }
    
    private func documentDirectory() -> String {
        let documentDir = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)
        return documentDir[0]
    }
    
    private func read(fromDocumentsWithFileName filePath: String) -> String? {
        if let dir = self.fileManager.urls(for: .documentDirectory, in: .userDomainMask).first {
            let fileUrl = dir.appendingPathComponent(filePath)
            do {
                print(1)
                let fileContents = try String(contentsOf: fileUrl, encoding: .utf8)
                print("fileContents: \(fileContents)")
                return fileContents
            } catch {
                print("Error reading saved file")
            }
        }
        return nil
    }
    
    private func write(text: String, toDirectory directory: String, withFileName fileName: String) {
        /*guard let filePath = self.append(toPath: directory, withPathComponent: fileName) else {
            print("Cannot append file from saver")
            return
        }
        do {
            try text.write(toFile: filePath, atomically: true, encoding: .utf8)
            print("Save Successful")
        } catch {
            print("Error writing to file")
            return
        }*/
        
    }
    
    private func fileDoExists(filePath: String) -> Bool {
        return self.fileManager.fileExists(atPath: filePath)
    }
    
    func getImage(imageURL: URL, completion: @escaping (Data?, Error?) -> (Void)) {
        return
        /*self.createDir()
        self.createFile()
        let imageURLPath = imageURL.absoluteString
        let cacheFilePath = self.cacheDir + "/" + self.cacheFile
        var cacheContent = self.read(fromDocumentsWithFileName: cacheFilePath)
        if cacheContent == nil {
            print("Unable to execute reader")
            return
        }
        var cacheDictionary = Functions().convertTextToDictionary(text: cacheContent!)
        if ((cacheDictionary?.containsKey(key: imageURLPath)) != nil){
            print("Using cached image")
            let data = Data(cacheDictionary![imageURLPath]!.utf8)
            completion(data, nil)
            return
        }*/
        
        URLSession.shared.downloadTask(with: imageURL) { localUrl, response, error in
            DispatchQueue.main.async {
                if let error = error {
                    completion(nil, error)
                    return
                }
                
                guard let httpResponse = response as? HTTPURLResponse, (200...299).contains(httpResponse.statusCode) else {
                    completion(nil, NetworkManagerError.badResponse(response))
                    return
                }
                
                guard let localUrl = localUrl else {
                    completion(nil, NetworkManagerError.badLocalUrl)
                    return
                }
                
                do {
                    let data = try Data(contentsOf: localUrl)
                    /*let dataString = String(decoding: data, as: UTF8.self)
                    cacheDictionary![imageURLPath] = dataString
                    cacheContent = Functions().convertDictionaryToText(dictionary: cacheDictionary)
                    if cacheContent != nil {
                        self.write(text: cacheContent!, toDirectory: self.documentDirectory(), withFileName: self.cacheFile)
                    }*/
                    completion(data, nil)
                } catch let error {
                    completion(nil, error)
                }
            }
        }.resume()
        
    }
    
}

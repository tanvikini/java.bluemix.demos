

 // (C) Copyright IBM Corporation 2017, 2017

 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at

 //      http://www.apache.org/licenses/LICENSE-2.0

 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.

package com.ibm.actions;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.AudioFormat;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;
import com.ibm.watson.developer_cloud.text_to_speech.v1.util.WaveUtils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.*;
@ManagedBean
@SessionScoped
public class TextAction {
	private String text;
	private String TEXTUSERNAME;
	private String TEXTPASSWORD;
	private String audioStr;

	public String getAudioStr() {
		return audioStr;
	}

	public void setAudioStr(String audioStr) {
		this.audioStr = audioStr;
	}

	public TextAction() {
		TEXTUSERNAME = System.getenv("TEXTUSERNAME");
		TEXTPASSWORD = System.getenv("TEXTPASSWORD");

	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public String navigate(){
		audioStr="";
		return "textHome";
	}
	
	//method for converting text to audio file and make it available for download 
	public void download() {
		System.out.println("Download");
		TextToSpeech service = new TextToSpeech();
		service.setUsernameAndPassword(TEXTUSERNAME, TEXTPASSWORD);
		try {
			FacesContext fc = FacesContext.getCurrentInstance();
			ExternalContext ec = fc.getExternalContext();
			ec.responseReset();

			InputStream stream = service.synthesize(text, Voice.EN_ALLISON, AudioFormat.WAV).execute();
			InputStream audiostream = WaveUtils.reWriteWaveHeader(stream);
			
			ec.setResponseContentType("audio/wav");
			ec.setResponseHeader("Content-Disposition", "attachment;filename=audio.wav");
			OutputStream out = ec.getResponseOutputStream();

			byte[] buffer = new byte[1024];
			int length=0;
			while ((length = audiostream.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}
			
			fc.responseComplete();
			audiostream.close();
			stream.close();
			out.close();
			System.out.println("done");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	//method for converting text to audio stream
	public void speak() {
		System.out.println("Speak");
		TextToSpeech service = new TextToSpeech();
		service.setUsernameAndPassword(TEXTUSERNAME, TEXTPASSWORD);
		try {

			InputStream stream = service.synthesize(text, Voice.EN_ALLISON, AudioFormat.WAV).execute();
			InputStream audiostream = WaveUtils.reWriteWaveHeader(stream);
			
			byte[] bytes = IOUtils.toByteArray(audiostream);
			audioStr = "data:audio/wav;base64,"+Base64.encodeBase64String(bytes);
			System.out.println("done");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}

package com.ks.plugin.widget.launcher.changba;

/**
 * Created by Administrator on 2018/3/5.
 */

public class BResult<T> {

    /**
     * result : {"items":[{"id":"027","name":"闪耀歌王","image":"http://aliimg.changba.com/cache/photo/dressup/6e0d9aaabd40770826a0716dedce652b.png","isnew":0,"viplevel":6,"category":"titlephotos","gender":1,"headphoto":"http://aliimg.changba.com/cache/photo/dressup/95f8b02acbfde75e528d4da6a9b11019.png"},{"id":"028","name":"水晶皇冠","image":"http://aliimg.changba.com/cache/photo/dressup/bf0e8adfb03adff8af0c44d3a9bd8528.png","isnew":0,"viplevel":6,"category":"titlephotos","gender":0,"headphoto":"http://aliimg.changba.com/cache/photo/dressup/c8a6e074343e432619305f9bd6d7131a.png"},{"id":"029","name":"金色荣耀","image":"http://aliimg.changba.com/cache/photo/dressup/91a919e76cebc62cca38feeadcba89bb.png","isnew":0,"viplevel":6,"category":"titlephotos","gender":0,"headphoto":"http://aliimg.changba.com/cache/photo/dressup/dc05eec454d16fa3ef633df2f850b6f9.png"},{"id":"024","name":"招财猫","image":"http://aliimg.changba.com/cache/photo/dressup/a477627c3fd06ba695202e9f1a9f2612.png","isnew":0,"viplevel":5,"category":"titlephotos","gender":1,"headphoto":"http://aliimg.changba.com/cache/photo/dressup/cb084cc36486ac555ea3ff39f75c6887.png"},{"id":"025","name":"花之恋","image":"http://aliimg.changba.com/cache/photo/dressup/3a524ed4f7856182d7292691398a1f40.png","isnew":0,"viplevel":5,"category":"titlephotos","gender":0,"headphoto":"http://aliimg.changba.com/cache/photo/dressup/0bfcae24e48100e8f9b5fb479cd77325.png"},{"id":"026","name":"王者之翼","image":"http://aliimg.changba.com/cache/photo/dressup/5decf2bbdda44b0a93ef9d32b7d0c1e8.png","isnew":0,"viplevel":5,"category":"titlephotos","gender":1,"headphoto":"http://aliimg.changba.com/cache/photo/dressup/3f68cde4b48e0831314fe5c23b277575.png"},{"id":"023","name":"海底漫步","image":"http://aliimg.changba.com/cache/photo/dressup/5a8d3764bebadc31bdf16250a2e515bd.png","isnew":0,"viplevel":4,"category":"titlephotos","gender":0,"headphoto":"http://aliimg.changba.com/cache/photo/dressup/22578006c8a232e8d3e8a6a9497dd6ab.png"},{"id":"035","name":"恋爱循环","image":"http://aliimg.changba.com/cache/photo/dressup/c6c230a58a9dfa80995ce615010bf0c9.png","isnew":1,"viplevel":4,"category":"titlephotos","gender":0,"headphoto":"http://aliimg.changba.com/cache/photo/dressup/17d8f99c2906999c7d431aa0582dbff8.png"},{"id":"005","name":"猫咪控","image":"http://aliimg.changba.com/cache/photo/dressup/69ffad67311096c0b32caedd872a3eca.png","isnew":1,"viplevel":3,"category":"titlephotos","gender":0,"headphoto":"http://aliimg.changba.com/cache/photo/dressup/db7b2843f07185543e71656d16b5dba0.png"},{"id":"038","name":"嘎嘣兔","image":"http://aliimg.changba.com/cache/photo/dressup/b9b4d87ba304b56fb1ba55210612dade.png","isnew":1,"viplevel":3,"category":"titlephotos","gender":0,"headphoto":"http://aliimg.changba.com/cache/photo/dressup/c50dd56f9a2751d020d3d5f38acdbee9.png"},{"id":"022","name":"敲可爱","image":"http://aliimg.changba.com/cache/photo/dressup/0d01ee9b0d9537d8b49d67a86692f2f7.png","isnew":0,"viplevel":2,"category":"titlephotos","gender":0,"headphoto":"http://aliimg.changba.com/cache/photo/dressup/e4375fb5be745a67556b74c75a1b891f.png"},{"id":"032","name":"求包养","image":"http://aliimg.changba.com/cache/photo/dressup/d9cd3d3efe9e04179f2967a5a2483e90.png","isnew":1,"viplevel":2,"category":"titlephotos","gender":1,"headphoto":"http://aliimg.changba.com/cache/photo/dressup/14437929c12cddab8409ac9da35b4015.png"},{"id":"021","name":"我爱唱歌","image":"http://aliimg.changba.com/cache/photo/dressup/3049b6be60674ce920c541cf24170cc9.png","isnew":0,"viplevel":1,"category":"titlephotos","gender":1,"headphoto":"http://aliimg.changba.com/cache/photo/dressup/bc483e72e6f27b15d1e71e8edafac7a0.png"},{"id":"030","name":"我是麦霸","image":"http://aliimg.changba.com/cache/photo/dressup/b740b2aa0b19f46bfc83bb412e19bee4.png","isnew":1,"viplevel":1,"category":"titlephotos","gender":1,"headphoto":"http://aliimg.changba.com/cache/photo/dressup/78ab34ee0e74e72c063db83ed8595dd0.png"}],"currdressid":"0"}
     * errorcode : ok
     */

    private T result;
    private String errorcode;

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public String getErrorcode() {
        return errorcode;
    }

    public void setErrorcode(String errorcode) {
        this.errorcode = errorcode;
    }
}

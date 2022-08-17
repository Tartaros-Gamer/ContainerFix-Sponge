package io.github.ahdg.containerfix.conf;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class Conf {

    @Setting(comment = "防止GUI保持的容器ID列表 \n" +
    " \\\"minecraft:chest\\\" # 箱子\n" +
    " \\\"minecraft:stone:3\" # 闪长岩(1.12)")
    public String[] AntiGUIKeepContainerList = {
            "minecraft:chest",
            "minecraft:stone:3"
    };

    @Setting(comment = "防止多开的容器ID列表 \n" +
            " \\\"minecraft:chest\\\" # 箱子\n" +
            " \\\"minecraft:stone:3\" # 闪长岩(1.12)")
    public String[] AntiMultiOpenContainerList = {
            "minecraft:chest",
            "minecraft:stone:3"
    };

    @Setting(comment = "在容器被打开时，破坏方块的行为，true 代表阻止方块破坏，false 代表强行关闭 GUI")
    public boolean PreventBreak = true;

    @Setting(comment = "容器多开时，向玩家发送的信息")
    public String MessagesMultiOpen = "该方块目前正在被其他玩家占用哦，请稍后再试";

    @Setting(comment = "容器正在开启时，另一名玩家破坏方块时发送的信息")
    public String MessagesAntiGUIKeep = "有人正在开这个容器啦，等别人用完再破坏这个方块吧";
}
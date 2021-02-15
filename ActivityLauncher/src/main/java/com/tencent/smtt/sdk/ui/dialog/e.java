package com.tencent.smtt.sdk.ui.dialog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Base64;
import com.tencent.smtt.sdk.MimeTypeMap;
import java.util.HashMap;

public class e {
   public static String[][] a = new String[][]{{".3gp", "video/3gpp"}, {".apk", "application/vnd.Android.package-archive"}, {".asf", "video/x-ms-asf"}, {".avi", "video/x-msvideo"}, {".bin", "application/octet-stream"}, {".bmp", "image/bmp"}, {".c", "text/plain"}, {".class", "application/octet-stream"}, {".conf", "text/plain"}, {".cpp", "text/plain"}, {".doc", "application/msword"}, {".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"}, {".xls", "application/vnd.ms-excel"}, {".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"}, {".exe", "application/octet-stream"}, {".gif", "image/gif"}, {".gtar", "application/x-gtar"}, {".gz", "application/x-gzip"}, {".h", "text/plain"}, {".htm", "text/html"}, {".html", "text/html"}, {".jar", "application/java-archive"}, {".java", "text/plain"}, {".jpeg", "image/jpeg"}, {".jpg", "image/jpeg"}, {".js", "application/x-javascript"}, {".log", "text/plain"}, {".m3u", "audio/x-mpegurl"}, {".m4a", "audio/mp4a-latm"}, {".m4b", "audio/mp4a-latm"}, {".m4p", "audio/mp4a-latm"}, {".m4u", "video/vnd.mpegurl"}, {".m4v", "video/x-m4v"}, {".mov", "video/quicktime"}, {".mp2", "audio/x-mpeg"}, {".mp3", "audio/x-mpeg"}, {".mp4", "video/mp4"}, {".mpc", "application/vnd.mpohun.certificate"}, {".mpe", "video/mpeg"}, {".mpeg", "video/mpeg"}, {".mpg", "video/mpeg"}, {".mpg4", "video/mp4"}, {".mpga", "audio/mpeg"}, {".msg", "application/vnd.ms-outlook"}, {".ogg", "audio/ogg"}, {".pdf", "application/pdf"}, {".png", "image/png"}, {".pps", "application/vnd.ms-powerpoint"}, {".ppt", "application/vnd.ms-powerpoint"}, {".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"}, {".prop", "text/plain"}, {".rc", "text/plain"}, {".rmvb", "audio/x-pn-realaudio"}, {".rtf", "application/rtf"}, {".sh", "text/plain"}, {".tar", "application/x-tar"}, {".tgz", "application/x-compressed"}, {".txt", "text/plain"}, {".wav", "audio/x-wav"}, {".wma", "audio/x-ms-wma"}, {".wmv", "audio/x-ms-wmv"}, {".wps", "application/vnd.ms-works"}, {".xml", "text/plain"}, {".z", "application/x-compress"}, {".zip", "application/zip"}, {".7z", "application/7z"}, {"", "*/*"}};
   static String b = "iVBORw0KGgoAAAANSUhEUgAAAEgAAABICAYAAABV7bNHAAAABHNCSVQICAgIfAhkiAAAFlFJREFUeJztnHuQbdlZ0H_fWnufR3efvrfvezLMkMgwJM4og3kYguhghBB5FAnGQi2pioZKRQENUqJ_UAWFaEmVVlkpi5cUhYAkGAghvBMehZWKAiGJmBCMkJg7NzM3d-7t27f7dPfZe6_v84-11t77nN7dt-eRIZZ-NXv69Dr7sdZvf-t7rXVbOEZum23p_uIbypH_SofdF9S-yIs77vT_-0TsA03Fkxtr5W8u4D-uiVwbPG21wczGCt_m4DuAM5_xjn4WSDBjf1H9kE7G_-ysyHb_uyVAe3t2ZX2ddwIve057-NkjV-uarxuN5PdzQwto3-zeKbzPjPv-dPr22SEi7NQ1fzVDEojTCvhts_9nNWdJRLg6n_OyjQ15wgEofNv_h9OJGfeVZfg-ALlttnUGPm72p2OQ5YibALPnvh-rohg78_rhwh9UrwyT8jmB44docBTIcdCU55acE_v7BaX7u5_Jh_ShmMFeo7z308aHHjM-eBM-fsu4sxDm6bR1g82x8YJzwqOX4OH74aXnPSMv-ORTnitYxUheJbvV4sPTovyzz-aNTaHw3WD-eFv50Q8rP_ExuHbTsApwIAXgVy4O6R4NyAG4AorSeOEVxxv_HPztL3TMStfeO6jxmYpf9-sa2Zkvdten5cYzuZFDUGwJTBOMH_8T5bv-i_HYdUMKkImwPjLKoZuEEx5Qwa2DCAzgJQ_CW75SeOmFAuhAeSfPqmbt1zVyZ7GwtXKwy6cW0_iz8IIZ_Mz_DrzhV5XdW-A2hLPTlU6HdHg6MP3PcCKwnW3QufDgg_Dzr3E8cC6qUBOeXW16VgCZgneCCDxZGQ-_teHGNSi2hM2JxYFmPkODPg7ESe0J5q05yB3h614O_-lro516NiG1gKb-6QFSM0ZF7M1bPhp487sUmQjnzxihTicZMRx9qpBWNeyEc7dvRVv1kW_2PHDO0QRDzXDHeM3TykGoedqsM5wmGF_1aw3_-B3K1hacnSU42fj24WRZNczHyXFa1L9e4fJZCB5e-D2Bf_fBisILo8Khz0JA9bQ0KMOpgvHgzwQee8y4cAFCskVHBtbXomeiQavn5ee59Flh-xq86cvhLX89jqlq9Glr0tPSoD6c-9-a4FyWDg50b7j_pm2grS9D7UPghs7rgbp8H3z_u-FbfqnGDEr_zDSpCGqoO_0NSu8IZrz4ZwM3bxHh1Hb0bfd_z9ozNN2ynOTm-9_nn6vak9qqHqTNWcP3fmkMBZ4OpKD21DVIBL7xtxr-8Kpx8YJB6MEZersn2aKh806S1XNW4GSpFLbuhX_9s8Yv_69F60iejpz6yjy13v6JwNs-EDVHawgC3hiGNOSFcvvqeUPnrMqq9ujK9265bXIZvubfO3YOw9OGJNsHh7ZWnGyk1YxJoew2ngs_omxODF8mwxeshdTaoeOM64qxvl0JdmhYszJYBzKGrXLl-rZDHNWeFTgAIwfXb0cv98k3F5g-tam239QUpznRiWCUvPo9DQDlGLSFIfgMyUWNOlZrAtQN7N2JTfeeh1d8nvBnZrA5im3XD-GPbhi_8xhs34jxzZnN7vrTwoE41S6fhetX4T-8P_DGFxuHjXtKXk22Dw5t4k_mNCmU994QHn0b0e4AqisPSZDiZwa1aHsHpBT-6SPwj14M50eK98Nv9LAp-MMbFd_730p-7gPxnK1z6ctTwAGQAOahrqHaNW59t2dSKIfN6abbYWjuDsiJ4L3wonc0fPI2XFgXVE8PyRts7wth1_jWlwn_4hWwVkRyB5WnVh00hNMSvBdA2Z4Lr_454ff-QNi6aB2UY-BI78WYj7_feiLFR6_mqQG6Od-3tXI0eIKpMR0Zv3tTeMVPweVLtHCynAQpw5mK8Xuv8zywVROCsFvHsQ11U9Vw4tCUAQczttbjDX_w_Z5_-J9hsmVMR5wIJ4PJUumyFh1UgriTp9p-Xd3dixkl3_X74NeNQsCt3NQ5w_XjKC94iwlshnP19cYDWzXbc2FeQ0Hv5Ztg4lC1HvxuZN47tufC9tx444sD7_kHgcNt4aA6PRyIBrs6EH7sQwZy-szBQdSUoWM6Mvarhl9_zDg37UOR9hgE5YXbi_j56uuNWWncPhRK38sK1ECjOxNtcJKudYKKa3-3RildVJUndwOP3g_v-qYIqQ8m25s-qFYy-Bm85X2BYIY4OXbc-WgBDUnW3p_-pGCVME3QC-mODGuJeAIV5sJvvBZmpbE9NwpTVI0QrOswoCbUGn8CWDBCUGqNL04N6kZBFQ_cuN3wFc9veNNfMbZvCKM0giGtIb8EJ6DG1kT56J847sxrJoVip3D5xwLKX7z940axfpeb9LTJOeHmjvA3Hza-5GJge25471oAInTaQoTgLFUFLX1QBSXZoWSL6OKsg33j37zygI2zxo292LYEpw8m_w6Ij6P66Y-6dM-7u_tjAY28chAcH7hpbCUb7p3he_amr0nZPmU78v0vN0IQHIIzXYKiJgSzdjUjeEM1gCp1e54StANjwTCLdup2Dd57fuBVFTrvDfIYMEvfj4Tf-FikKWrHRQjLgHTg8F7YrYydRloIWTKoDKsP6dYBfNULYXNi3N5XMKNWqJUWSjDDnFGjqIA00lZhJfSgWJxuISiNBhoNBANCw6d3Aq99KLBx1ri9WBlVD4yZtQcAY-M3PyGEYBSp-D80_jYXVgUxQ9INLFh7_4_cCITaGBVG0s725yqs_Nkq4VtfEDr6gLN4oNpqijWCV0ODEVQREywYjUYoWVvibFPMHPHSGM0vqvjMVz1foxYlzekDMTNkadnJOONr9nYOmS8k2qEQM_bMoH-orkwxC2muakyO_uCgwJeCTyrbh7MKzDtjEYRiHV5y0VhU3XtQ0zauib8LphozB40wGk1GRBXVqDGKokFbMMECarGcaihSK6_5gqbXf10CAqAhYKrtAVDtCZ_a6xLAk6aZg2isVAExEKNJVe9r-xFY6eKSyiqo_k_v4NDghVswHQUOK0HpAr5spNupY3HqKBYPiZA0pR5qoAEMbcFkCRrDhJ3DwCPn9xl5RUMYBGLB2qMvTxzGUFUlfnecwS4gGiuScVTApTl2EGjde5ZJAfVq9EwsLgFcSQa9yekIgokhxGUZSNFyz6Br7x1aIxhRvQVtwfQrlvm6qjE2x114AHoERF-szisJwhM3G7i_yyDcMfWqAjXwnTV3ycN0gxTKlavLgQpk6YAKzpbJlqV7iETbYnRw4r2znXKYOUwVcQ5De3AcEJbLuS0kBQ-jQkAPOTiA6fhYNseLxtzOQhyhc71KBVAEeq5OrYUTgjDxwsj6kIalVqN0wkQgpCmVDaSqogjZ4inRJgkOEWiC4jyYI06JeDbBQAiodhlplyR3I6iaVVfexBpJ_jwkTQXM4jjT1M_mtZblWl2XxquRE4HYxZq1YoJzMPVxuq0W5RqNbUWCNxvDbuMJYYFI0cIixxsavZEQNcVCzsciHMGhlr7TvIy8DGdV7qSUZuIPQdNSdHMIgLjjqxSbo5xKpCqeGJjgV6LxpTuYRTjxDZVcIJDZTj3UBmVPkcoe6sIZTSE82UC1cLF4VkfcLultHKPm_3AOXMwiWq3JcJxTVPOU62yWqrb2blQ4PvRJqG7vMznvsBWNWf29L_eci51XUrjhBCfxRTrr5ZiQo1S6qQA0IfDgRszgi2Rzpt4o3NFjnDCfHcOdCj6255loTRWixjTZiGjUlHb5LEBjihk0QVo4lqNo7V7n4WGgbgKGZzwqmE5KNmcl778WsN1t7jy-S2ji-aEJ7REbukgyt10ch5QXxuej0ibQOSwJqhRq1u2I0BRjJLvzgpkyHUXSEcKyHSrbtMAofdSotQJ--9Oehx8wXCWoNgiOkDySE1nyTl7ierpzEYp3tmSUmyYQ1Jitl2zO1piMyqidKYj7l1874ztffZ4feu8u3_GTtwDYvNSrb4WmhRQsvsn1M8bFDc9BpUljUlw1YGZbq-KMpbCxXgQ-ZxraWnEptnSsFVB6aY-1QigFnrcO79l2NI1E44vrOXGPmlH3XLGatdMJuhgHOmP8vEtnuOfiGcrS02hg0YT2Z23Kxrjgn7xyi70ffD4veVHJztW9oyMFvDTMd4QXXagpvGdRx-RYJT6znx8eARQDM2trKofqGI2Vz9-ERmUJRukjjHzkqbdWwJkS9ht4540xF8ZRfQWNOVQqhHkHqi5NI6gba31ptjdNmgr3XdlibVSyX9dHyhPiorfcrxp2q4px4Xnft9_Pax6dRUhad0eWas5ffiDGA40aiKAhPtMZcZ9T7zEdIGIZQlIQWCc9_0sXowcbApKhjJykI_7-uevGL1wveHLhWPN1NL4qGNJCWQXSryjmaXXl4tk4prrBDwSnfVBeHPtVg2K87Q338tJH1tm51stie6Be-4UToAZS-iFCE2LiHB1Issm2srKqJjQEzATnojd69GydXHkHpQzLUDKYwgmlGLNSWCvgh6-tsV4UeKH1Ru2zekD67UGFoMbGxpTSC1U2tF7ghCg5g9qva7wIv_LNV-K9m3rp2LwgfNG9I27tRv0IpjQpOQ45VkuQOg1KRsKJpTcVG-Y1fN76gofOGgdBWihSdmAKJy2YqYeJF6YeLk-Emwv4sevrnB9Jm7ct156jnVFV6iZ6qZytn5mNqJqAA4IzpHV9J0Py4thZ1JydjnjNozN2r8cLXVGye135O4-MKLznsO6eFT2ZgEJoNO8SxehrkC17D7Try-suKfsNLZQMZuppwRQu1o1KB6UTSmc8bypcOxDeeWNMYYHxSk00G2HnHGuTMbP1MWUR3XjhcpwSa0S1Hs3Uj5Miedu_98Vnsf15vE8Tp9e3fOk6B_sVsXjZhSCqYcnGKWkzatsiEleFLbuzgAjcWhQ8euGQn3xinUrj1Inu3doIehUMGOP0-d4p3Aglv7q7xZdM7rBRBubJZjrnKDZnrBfC1MWajJ81BCtYNA0uhxXWBXECacPEybDM4C_eH4d35-O7ALzsFZf43EsTHr9V4b0Q0ppccGlvtloMarE2oBksufZ3igaDojC--nLDTk2rLRMfNWbql-GMnbRw1jyMnXB-HOOo_x42-Z-LmAMZnr0zW0CM0BehpjGjDoJqaKNZB2iC09AlwCeJODhoGs6sj_nIDzzET_3z57P5ghn_6mvWqRcpmGyD4w5org71NUk-dXvXJlKknMhSrYaYGvQobvrAt__RGrUJs6R3GUz83GlNmWzS2EkbZc-KlPEDkxImHjZ8nJ5jB1NneFGcSfvW-s_Pn0UcTjj1Rs21stsqrKHijx-f451RFA4nLoYhAoWPU04kKoCIsLAwrEGOFBe4FLwloG-4r2G_iQNdK6TVmo2CJTgQf88ykg5O4aFMmlBbPOIzBJLWqHY5kQKNHDXMpkeaBmW-aJgvGg6ahiAl91_Z7J5hMXq2VJyLY1_2qkcAiXOdCicjKgLbjefhzQWvvCzcqkgwrAXR15yNFGyOiwhn7Ds4Q9t0cvIZECzdTyVWGePzu4tie-rXKSCJ67StbgJOhIvnNqiTA8uzSSFG1OR2i0nzEqmMM7c4Wi2CaLC_4eIen7MWo-UhOLmtD6ccgOJ787xB2sXmRkGdEKxLUSxplEkirS5G_qeE1IfVmLI-KZmteZpm-WIJ2gPWqxOtro6aCSIWvYjGcoUkusF53nTfgtIJC7VTwynussUuGFS5bp061-CWasVmqe6ctEt7RvapSBWU9UlMN1btvawQH7RB4jtv4VwHCeB2HVONN9530Namy56NGJ-w1ahwnf2J16X21JYhhXRkCciRonq_ZnNae7TUl5HHORn8t2lKtMNiK1tz8jQTE6SdWm4JUulizee8D3zTPXNqFWqTJY81pD192zNyRzUq9NON9FOwFpYnrjyYGphrp0C-7KlAyuVg7_yRMoeZS14z7jrpbJCTVp-cxDJANNi2BIlUS95uPOemjm-8Z04pxiJX-XpGuQ-nxJj4YTjHSdYiL92yTEBo8lLSM4CUXXPPDKfxJgZLuVgrmV7eMmGtPYrTzccQgLhv504Fs9Lx9ZcW3LsmLHoVznIAhDdr2_L0KiUaaYhaFCz2PS9Fd-PJUXvq2zHW-VSeTYTGojuXpBc-zZhVm7MyxSw7L5AY3jtHO9362uSIuzZ2U9rw12Z7PLJ-dFdT1h4_kBoUrouDsrQuP8dHdHCCCbVZTAQs59nLWnQaz-bF0VSxr8vjy7XyGCiKG9hI7pAWkuKWplsWRdo19ADMDxuuH8BlN-fP-zvMtDlid0I4apybYwYyBKkvgsVpJsNT7ThIplF7gil7hw2ll7T81J28CqQY2hLrENRZXHEgJm8aQowwnaOpmzgNNFA3sRBeB6OqA6YVk2bOQsHOXYRp0WpPyKP2y6sj0E0ziKlHCyvlpf096jmVbNSO7DzJoul_eUaaQll4vIOrT9xBnFEURevFRGLM54jmRETAiEX7IcmQyPtyiCuuTRUNzSqcXLoIjbJQWCwa5NpVKhx27gyjc2fYWOuMUgB8Kob1l5MKLBXvs4EeBgDJHqX8rQVj3fYgBwhCmVS5CspHPvYkAOfOTKH1YP3sz9NfRS7MeBy4Z7gLsQMCcZoJFD7WUASHdwF1RolQ4yhQrHCMG4VxQVDPaLFgfv1Jdj91g9uTEdNxydrWJn5znWkKC8Y9a56XmNoBs5zXAYyXcjNPIXmFZCCsM2O-X_GJx-_wxJNzNqcF587lLXPd-Z32pC0zIjiBQpz8DxHuGVKknLTmFVScp1HD-7imFVQoi4K6iTXj0AScc2ipFCFpWzFhpMlDLRbs7-2ztxOLWC6px2g8ptZhg1S6gUEfI0VvJTNrYFCjqQNF6bl0bsradMRk5ElJfruo2U4t59p0y5DHi0lRvAPjy7Oq2UAwEQtjcSl4JHFbS-HTWroJ41GR9vmUsfitRtBA0BjOqzaYCnWYpN_z4qS1e3aGJDuG4i7FsSw52i_T-eIs2R0fDXI_znGpiKLa81oJjsToaDYp3yV7e3alCoePey-oEyQYIsOgsihdEpTrSIi0g437fmJzbusXpgYHJ9pWgJ2j3dwQk8X4vxy8HXn-6r167Xm3xnJW7tpKhXM-hS90U4vo6g-a-ssEYHvv8N86eDNwekhp24j2R54gmcWq4PJXcadYP1yIb1Nzr3PmnD7bUiWhL7niZ9YtMOQ9BX04LZMVKNDFOi2Y1CHnooMS5959Zn30FQLxz3HJvPog2P0tACc4lbtoUruQNKhJ7T7D1Q7Tf8M54hJiGpPeZt8WeOIGg563Wn5OD9Qxu0C6isXwM5bhyF6jvPz8bPzh9ok3dxcPFY7_iln7Vxie6pRTwJwhcZ9m25HhDdtuSbVNDMl1aJGlcmv3nP7V2m0-793_pM3hWVPyul__WRkOgPfyutna-O2wshuhquwvHFTVL4Jd6bef9t-gtQuBp-jwUuFdesWpAckrr1kLltbVjunkUuH9uCJ_77nORc1xwuszHFjdrkH8O2ZlGb7vsG4G_yrM3WB1-w7vcsEKlNWi3WnkyL88OuV1_ReRnyvOvbtRe_P52fjDS9087iY3dxcP1Vr9LUH-hsEXDHbwbrSs23N9pJP5yc_wryMMPfOu0k41Hl8ry7ce1vXP37M1-62hU_8PhhGA3eX3jsoAAAAASUVORK5CYII=";
   static String c = "iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAAAAXNSR0IArs4c6QAABEJJREFUaAXtV01oXFUUPve9ZKadjIumixY3NhREEBHrqiWbQCHzEwjSNghJzbyJyWvBFBcuRKil3dhNK3QhM_mZN2nVxQiCmHmTojL-pS6k6qILFVtKi5iNbYlNsOl793ou6R1u3tzMvBkTZhbvEXLPPff8fOe7vwMQfAEDAQMBAwEDAQMBA61jgLQudf3MM3P2Sy5lcwzY0wTIKdNIfuz10ryKdulP5Ur9jku_ZYy9AAx2YxGXVNjasoBMvmhQoPMIOCpA41JZErLctl0BU_n5d4GyHADrEEAR_CoB3RB9ua0YycpWyOVyueO326sZStmYnJ8QcBlor5pG_EdZL-S22MSFQjl6_-FqAdd5XACrtIScPGEkM5W-R2h5AZZV3LsGrMgYHPBgA9DIeydSyXeq9JKipQVkravPMXBKwNg-CdO6SOAjM5U8TghhVWOSomWbOHu51AvMWVSBxzO_3N3Vk64HntexaQGFwrWdUqFbKk7P2UfBpV_gmu9WBL4B0dArQ0PPrynGqlTKJZSx5idx4s4DIX9poJ0aN-J2lWeTiqxlv8kYvYDu1eQR-DPEOg-m0_13_YavDoKeCP5tXHgRvAX38wslk7PPoqy09ZsI_Uk2V7yI4N9XgyfLuqYnGgHPc6tnIFecw4vkNRkcrscFvUsffn0odk_W-5Ft2w7fWaIfou1RlT2u-cdEZ4mJ0YEvVeO1dEpWoyEyibX9LDsigzFnxb0-nZ9_WdbXk2cKC913lygHpgS_7q-NNwOe-yoLGBlJLOs7wnFk_eZ6gif_8bhzKVnEIjbclhtspI5llfa5K84iLsdeSe0RyWkzHccZb-5TLiERKpu39wOlHMAeoau0hMyGIfKGYfT9W9FJQja3cIARt4jH5F5JvUEkGpnBs358g7LBjnIGRAwzlbiJsxDD02hZ6CotY2NrsLLIWa7onggZqxjDC-qbmuCBlJ59JnLS69tov2YBPNiEkfxFB20Q98Qjb3B-_a8xep0DFmNTlp3G9_vn2K88hcWYaPGB9tOuaGSor6_PEbpm25pLSA46lS8ewZdiAXWqoinO1DkcI7jZz8h-VTIht8MABw0jqXzfV9nXUfgugMfBc9zE23PTl2GdXPzQvk-g85Bp9P9a19angYrNTV3NdDKraVCb4U29ySMdOga3EjxP1VAB3GEiNXAON_UHXPb74fJi-Hd83Ih959fHr13DBfDAeDpN4kb8xG8SPIbfMo0B3_Z-43K7pgpARumurp4RLOKresnwmXAJf1FdrGfX7HhDm9ibZPaz759y7j34WvlrCo2x0E8nUoljvGCv71b1m5oBkXxssPefnXpnHFn4Q-hEi7ofQhAZ3k7wPNf_mgEBdvry1R7Xda6JmxdB_75DCx8aHT38t7DZrnZLCuDgZq8svPjYcTMEWKgzBMfSwwO3tgt0EDdgIGAgYCBgIGAgYKBdGPgPk7xNaPuEPzwAAAAASUVORK5CYII=";

   public static Drawable a(String var0) {
      Bitmap var1;
      if ("application_icon".equals(var0)) {
         var1 = c(b);
         return new BitmapDrawable(var1);
      } else if ("x5_tbs_activity_picker_check".equals(var0)) {
         var1 = c(c);
         return new BitmapDrawable(var1);
      } else {
         return null;
      }
   }

   public static String b(String var0) {
      if ("x5_tbs_wechat_activity_picker_label_recommend".equals(var0)) {
         return "46种文件，极速打开";
      } else if ("x5_tbs_wechat_activity_picker_label_download".equals(var0)) {
         return "下载";
      } else if ("x5_tbs_activity_picker_recommend_to_trim".equals(var0)) {
         return "\\(推荐\\)";
      } else if ("x5_tbs_activity_picker_recommend_with_chinese_brace_to_trim".equals(var0)) {
         return "（推荐）";
      } else if ("x5_tbs_wechat_activity_picker_label_always".equals(var0)) {
         return "总是";
      } else {
         return "x5_tbs_wechat_activity_picker_label_once".equals(var0) ? "仅此一次" : null;
      }
   }

   public static Bitmap c(String var0) {
      try {
         byte[] var1 = Base64.decode(var0, 10);
         Bitmap var2 = BitmapFactory.decodeByteArray(var1, 0, var1.length);
         return var2;
      } catch (Exception var3) {
         var3.printStackTrace();
         return null;
      }
   }

   public static String d(String var0) {
      HashMap var1 = new HashMap();
      var1.put("3gp", "video/3gpp");
      var1.put("chm", "text/plain");
      var1.put("ape", "audio/x-ape");
      if (!TextUtils.isEmpty(var0)) {
         String var2 = (String)var1.get(var0.toLowerCase());
         if (!TextUtils.isEmpty(var2)) {
            return var2;
         } else {
            MimeTypeMap var3 = MimeTypeMap.getSingleton();
            String var4 = var3.getMimeTypeFromExtension(var0);
            return !TextUtils.isEmpty(var4) ? var4 : "application/" + var0;
         }
      } else {
         return null;
      }
   }

   public static String e(String var0) {
      String var1 = "*/*";
      String var2 = var1;
      String var3 = var0.substring(var0.lastIndexOf(".") + 1);
      String var4 = "." + var3;
      if (TextUtils.isEmpty(var3)) {
         return var1;
      } else {
         for(int var5 = 0; var5 < a.length; ++var5) {
            if (var4.equalsIgnoreCase(a[var5][0])) {
               var2 = a[var5][1];
               break;
            }
         }

         if (var1.equalsIgnoreCase(var2)) {
            var2 = MimeTypeMap.getSingleton().getMimeTypeFromExtension(var3.toLowerCase());
         }

         if (var2 == null) {
            var2 = d(var3);
         }

         return var2;
      }
   }
}

package shadcnscalajs.site.create.preview02

import com.raquo.laminar.api.L.*

/** preview-02 mosaic grid — 33 card stubs in the reference layout. */
object Preview02:

  private val columnCls =
    "flex flex-col p-1 [contain-intrinsic-size:380px_1200px] [content-visibility:auto]"

  private val wideColumnCls =
    "col-span-2 flex flex-col p-1 [contain-intrinsic-size:760px_1200px] [content-visibility:auto]"

  def apply(): HtmlElement =
    div(
      cls := "overflow-x-auto overflow-y-hidden bg-muted contain-[paint] [--gap:--spacing(4)] 3xl:[--gap:--spacing(12)] md:[--gap:--spacing(10)] dark:bg-background style-lyra:md:[--gap:--spacing(6)] style-mira:md:[--gap:--spacing(6)]",
      div(
        cls := "flex w-full min-w-max justify-center",
        div(
          cls := "grid w-[2400px] grid-cols-7 items-start gap-(--gap) bg-muted p-(--gap) md:w-[3000px] dark:bg-background style-lyra:md:w-[2600px] style-mira:md:w-[2600px] *:[div]:gap-(--gap)",
          dataAttr("slot") := "capture-target",
          div(
            cls := columnCls,
            ContributionHistory(),
            EmptyDistributeTrack(),
            QrConnect(),
            DividendIncome(),
            IndexInvesting(),
            SyncingState()
          ),
          div(
            cls := columnCls,
            PayoutThreshold(),
            ClaimableBalance(),
            Preferences(),
            SavingsProgress(),
            KitchenIsland()
          ),
          div(
            cls := wideColumnCls,
            SavingsTargets(),
            RecentTransactions(),
            div(
              cls := "grid grid-cols-2 items-start gap-(--gap)",
              div(
                cls := "flex flex-col gap-(--gap)",
                SidebarNav(),
                Faq()
              ),
              div(
                cls := "flex flex-col gap-(--gap)",
                Payments(),
                FrontDoor()
              )
            ),
            ReleaseCatalog()
          ),
          div(
            cls := columnCls,
            AccountAccess(),
            CardOverview(),
            TransferFunds(),
            CoverArt(),
            LoadingCard()
          ),
          div(
            cls := columnCls,
            ReceivingMethod(),
            PowerUsage(),
            EmptyConnectBank(),
            UpcomingPayments(),
            RollerShades()
          ),
          div(
            cls := columnCls,
            StockPerformance(),
            EmptyExploreCatalog(),
            NewMilestone(),
            SocialLinks(),
            NotificationSettings()
          )
        )
      )
    )
